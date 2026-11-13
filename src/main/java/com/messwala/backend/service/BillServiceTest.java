package com.messwala.service;

import com.messwala.entity.Bill;
import com.messwala.entity.PaymentStatus;
import com.messwala.exception.BadRequestException;
import com.messwala.exception.ResourceNotFoundException;
import com.messwala.repository.BillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    BillRepository billRepository;

    @InjectMocks
    BillService billService;

    @Test
    void markPaid_throwsWhenBillNotFound() {
        when(billRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> billService.markPaid(99L));
    }

    @Test
    void markPaid_throwsWhenAlreadyPaid() {
        Bill bill = new Bill();
        bill.setPaymentStatus(PaymentStatus.PAID);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> billService.markPaid(1L));
        assertEquals("Bill already paid", ex.getMessage());
        verify(billRepository, never()).save(any());
    }
}