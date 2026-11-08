package com.messwala.backend.service;

import com.messwala.backend.dto.BillResponse;
import com.messwala.backend.entity.Bill;
import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.PaymentStatus;
import com.messwala.backend.entity.Plan;
import com.messwala.backend.entity.Subscription;
import com.messwala.backend.entity.User;
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
}