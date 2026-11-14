package com.messwala.backend.service;

import com.messwala.backend.dto.BillResponse;
import com.messwala.backend.dto.DueResponse;
import com.messwala.backend.dto.DuesSummaryResponse;
import com.messwala.backend.entity.Bill;
import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.PaymentStatus;
import com.messwala.backend.entity.Plan;
import com.messwala.backend.entity.Subscription;
import com.messwala.backend.entity.User;
import com.messwala.backend.exception.BadRequestException;
import com.messwala.backend.exception.ResourceNotFoundException;
import com.messwala.backend.repository.BillRepository;
import com.messwala.backend.repository.SkipRepository;
import com.messwala.backend.repository.SubscriptionRepository;
import com.messwala.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class BillService {

    private static final int DAYS_IN_MONTH = 30;

    private final SubscriptionRepository subscriptionRepository;
    private final SkipRepository skipRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;

    public BillService(SubscriptionRepository subscriptionRepository,
                       SkipRepository skipRepository,
                       BillRepository billRepository,
                       UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.skipRepository = skipRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
    }

    // ---------------- Day 14: bill generation ----------------

    @Transactional
    public List<BillResponse> generate(YearMonth month, boolean giveCredit) {
        LocalDate first = month.atDay(1);
        LocalDate last = month.atEndOfMonth();
        String monthKey = month.toString();

        List<BillResponse> result = new ArrayList<>();

        for (Subscription subscription : subscriptionRepository.findAll()) {
            boolean started = !subscription.getStartDate().isAfter(last);
            boolean notEnded = subscription.getEndDate() == null
                    || !subscription.getEndDate().isBefore(first);
            if (!started || !notEnded) {
                continue;
            }

            Bill bill = billRepository.findBySubscriptionIdAndMonth(subscription.getId(), monthKey)
                    .orElse(null);

            if (bill != null && bill.getPaymentStatus() == PaymentStatus.PAID) {
                result.add(toResponse(bill));
                continue;
            }

            if (bill == null) {
                bill = new Bill();
                bill.setSubscription(subscription);
                bill.setMonth(monthKey);
            }

            int skipped = (int) skipRepository
                    .countBySubscriptionIdAndDateBetween(subscription.getId(), first, last);

            bill.setSkippedDays(skipped);
            bill.setAmount(calculate(subscription.getPlan(), skipped, giveCredit));

            result.add(toResponse(billRepository.save(bill)));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<BillResponse> myBills(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return billRepository.findBySubscriptionCustomerId(customer.getId()).stream()
                .sorted(Comparator.comparing(Bill::getMonth).reversed())
                .map(this::toResponse)
                .toList();
    }

    // ---------------- Day 15: payment status and dues ----------------

    @Transactional
    public DueResponse markPaid(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id " + billId));

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Bill already paid");
        }

        bill.setPaymentStatus(PaymentStatus.PAID);
        bill.setPaidOn(LocalDate.now());
        return toDueResponse(billRepository.save(bill));
    }

    @Transactional(readOnly = true)
    public DuesSummaryResponse getDues() {
        List<DueResponse> dues = billRepository.findByPaymentStatus(PaymentStatus.PENDING)
                .stream()
                .map(this::toDueResponse)
                .toList();

        BigDecimal total = dues.stream()
                .map(DueResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DuesSummaryResponse(dues.size(), total, dues);
    }

    // ---------------- helpers ----------------

    private BigDecimal calculate(Plan plan, int skipped, boolean giveCredit) {
        BigDecimal price = plan.getPricePerMonth();

        if (!giveCredit || skipped == 0) {
            return price.setScale(2, RoundingMode.HALF_UP);
        }

        int mealsPerDay = plan.getMealType() == MealType.BOTH ? 2 : 1;

        BigDecimal credit = price.multiply(BigDecimal.valueOf(skipped))
                .divide(BigDecimal.valueOf((long) DAYS_IN_MONTH * mealsPerDay), 2, RoundingMode.HALF_UP);

        return price.subtract(credit).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BillResponse toResponse(Bill bill) {
        Subscription subscription = bill.getSubscription();
        Plan plan = subscription.getPlan();
        return new BillResponse(bill.getId(), subscription.getId(),
                subscription.getCustomer().getName(), plan.getName(), bill.getMonth(),
                plan.getPricePerMonth(), bill.getSkippedDays(), bill.getAmount(),
                bill.getPaymentStatus(), bill.getPaidOn());
    }

    private DueResponse toDueResponse(Bill b) {
        Subscription sub = b.getSubscription();
        return new DueResponse(
                b.getId(),
                sub.getCustomer().getName(),
                sub.getCustomer().getPhone(),
                sub.getPlan().getName(),
                b.getMonth(),
                b.getAmount(),
                b.getSkippedDays(),
                b.getPaymentStatus().name(),
                b.getPaidOn()
        );
    }
}