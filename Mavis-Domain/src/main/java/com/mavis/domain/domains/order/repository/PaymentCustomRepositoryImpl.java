package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.mavis.domain.domains.order.domain.QPayment.payment;

@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Payment> findExpiredVirtualAccountPayments(LocalDateTime now) {
        return queryFactory.selectFrom(payment)
                .join(payment.order).fetchJoin()
                .where(
                        payment.order.orderStatus.eq(OrderStatus.WAITING_FOR_DEPOSIT)
                                .and(payment.paymentType.eq(PaymentType.CONFIRM))
                                .and(payment.virtualAccountInfo.virtualAccountDueDate.lt(now))
                )
                .fetch();
    }
}
