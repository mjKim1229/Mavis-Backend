package com.mavis.domain.domains.order.implement;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.exception.PaymentNotFoundException;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentReader {

    private final PaymentRepository paymentRepository;

    public Payment findByOrder(Order order) {
        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> PaymentNotFoundException.EXCEPTION);
    }

    public boolean existsByPaymentKey(String paymentKey) {
        return paymentRepository.existsByPaymentKey(paymentKey);
    }
}
