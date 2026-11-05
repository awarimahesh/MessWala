package com.messwala.backend.service;

import com.messwala.backend.dto.SkipRequest;
import com.messwala.backend.dto.SkipResponse;
import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.Plan;
import com.messwala.backend.entity.Skip;
import com.messwala.backend.entity.Subscription;
import com.messwala.backend.entity.SubscriptionStatus;
import com.messwala.backend.entity.User;
import com.messwala.backend.repository.SkipRepository;
import com.messwala.backend.repository.SubscriptionRepository;
import com.messwala.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyCollection;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SkipServiceTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final String EMAIL = "customer@test.com";

    @Mock
    private SkipRepository skipRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    private Subscription subscription;

    @BeforeEach
    void setUp() {
        User customer = new User();
        customer.setId(1L);
        customer.setEmail(EMAIL);

        Plan plan = new Plan();
        plan.setId(1L);
        plan.setMealType(MealType.LUNCH);

        subscription = new Subscription();
        subscription.setId(10L);
        subscription.setCustomer(customer);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.of(2026, 11, 1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));
        when(subscriptionRepository.findFirstByCustomerIdAndStatusIn(eq(1L), anyCollection()))
                .thenReturn(Optional.of(subscription));
        when(skipRepository.existsBySubscriptionIdAndDateAndMealType(anyLong(), any(), any()))
                .thenReturn(false);
        when(skipRepository.save(any(Skip.class))).thenAnswer(invocation -> {
            Skip skip = invocation.getArgument(0);
            skip.setId(100L);
            return skip;
        });
    }

    private SkipService serviceAt(LocalDateTime now, String cutoff) {
        Clock clock = Clock.fixed(now.atZone(IST).toInstant(), IST);
        return new SkipService(skipRepository, subscriptionRepository, userRepository, clock, cutoff);
    }

    private SkipService serviceAt(LocalDateTime now) {
        return serviceAt(now, "21:00");
    }

    private ResponseStatusException assertRejected(SkipService service, SkipRequest request, HttpStatus expected) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.skip(request, EMAIL));
        assertEquals(expected.value(), ex.getStatusCode().value());
        verify(skipRepository, never()).save(any(Skip.class));
        return ex;
    }

    @Test
    void skipTomorrowBeforeCutoff_succeeds() {
        SkipService service = serviceAt(LocalDateTime.of(2026, 11, 4, 20, 30));
        SkipRequest request = new SkipRequest(LocalDate.of(2026, 11, 5), MealType.LUNCH);

        SkipResponse response = service.skip(request, EMAIL);

        assertEquals(LocalDate.of(2026, 11, 5), response.date());
        assertEquals(MealType.LUNCH, response.mealType());
        assertEquals(10L, response.subscriptionId());
        verify(skipRepository).save(any(Skip.class));
    }

    @Test
    void skipOneMinuteBeforeCutoff_succeeds() {
        SkipService service = serviceAt(LocalDateTime.of(2026, 11, 4, 20, 59));
        SkipRequest request = new SkipRequest(LocalDate.of(2026, 11, 5), MealType.LUNCH);

        SkipResponse response = service.skip(request, EMAIL);

        assertEquals(LocalDate.of(2026, 11, 5), response.date());
        verify(skipRepository).save(any(Skip.class));
    }

    @Test
    void skipAfterCutoff_isRejectedWithClearMessage() {
        SkipService service = serviceAt(LocalDateTime.of(2026, 11, 4, 21, 30));
        SkipRequest request = new SkipRequest(LocalDate.of(2026, 11, 5), MealType.LUNCH);

        ResponseStatusException ex = assertRejected(service, request, HttpStatus.BAD_REQUEST);

        assertTrue(ex.getReason().contains("Skip cutoff passed"));
    }

    @Test
    void skipExactlyAtCutoff_isRejected() {
        SkipService service = serviceAt(LocalDateTime.of(2026, 11, 4, 21, 0));
        SkipRequest request = new SkipRequest(LocalDate.of(2026, 11, 5), MealType.LUNCH);

        assertRejected(service, request, HttpStatus.BAD_REQUEST);
    }

    @Test
    void skipTodaysMeal_isRejected() {
        SkipService service = serviceAt(LocalDateTime.of(2026, 11, 4, 9, 0));
        SkipRequest request = new SkipRequest(LocalDate.of(2026, 11, 4), MealType.LUNCH);

        assertRejected(service, request, HttpStatus.BAD_REQUEST);
    }

    @Test
    void duplicateSkip_isRejected() {
        when(skipRepository.existsBySubscriptionIdAndDateAndMealType(
                10L, LocalDate.of(2026, 11, 5), MealType.LUNCH)).thenReturn(true);
        SkipService service = serviceAt(LocalDateTime.of(2026, 11, 4, 20, 30));
        SkipRequest request = new SkipRequest(LocalDate.of(2026, 11, 5), MealType.LUNCH);

        assertRejected(service, request, HttpStatus.CONFLICT);
    }

    @Test
    void customCutoffSetting_isRespected() {
        SkipService service = serviceAt(LocalDateTime.of(2026, 11, 4, 19, 0), "18:00");
        SkipRequest request = new SkipRequest(LocalDate.of(2026, 11, 5), MealType.LUNCH);

        assertRejected(service, request, HttpStatus.BAD_REQUEST);
    }

    @Test
    void pausedSubscription_cannotSkip() {
        subscription.setStatus(SubscriptionStatus.PAUSED);
        SkipService service = serviceAt(LocalDateTime.of(2026, 11, 4, 20, 30));
        SkipRequest request = new SkipRequest(LocalDate.of(2026, 11, 5), MealType.LUNCH);

        assertRejected(service, request, HttpStatus.BAD_REQUEST);
    }

    @Test
    void mealNotInPlan_isRejected() {
        SkipService service = serviceAt(LocalDateTime.of(2026, 11, 4, 20, 30));
        SkipRequest request = new SkipRequest(LocalDate.of(2026, 11, 5), MealType.DINNER);

        assertRejected(service, request, HttpStatus.BAD_REQUEST);
    }
}