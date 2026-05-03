package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.order.dto.*;
import com.mavis.api.order.implement.OrderItemAppender;
import com.mavis.common.util.OrderNumberGenerator;
import com.mavis.domain.domains.order.domain.*;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.domain.RefundType;
import com.mavis.domain.domains.refund.implement.RefundAppender;

import java.util.List;
import com.mavis.domain.domains.order.exception.CannotCancelOrderException;
import com.mavis.domain.domains.order.exception.InvalidOrderInfoException;
import com.mavis.domain.domains.order.exception.OrderNotFoundException;
import com.mavis.domain.domains.order.exception.PriceMismatchException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.order.implement.PaymentIdempotencyManager;
import com.mavis.domain.domains.order.implement.PaymentReader;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsStatus;
import com.mavis.infrastructure.outer.api.tosspayments.dto.VirtualAccountDepositCallbackRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserReader userReader;
    private final OrderItemAppender orderItemAppender;
    private final PaymentRepository paymentRepository;
    private static final int DELIVERY_FEE = 4000;
    private final OrderReader orderReader;
    private final RefundAppender refundAppender;
    private final PaymentIdempotencyManager paymentIdempotencyManager;
    private final PaymentReader paymentReader;

    @Transactional
    @Retryable(
            retryFor = {DataIntegrityViolationException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        User user = userReader.getCurrentUser();
        OrderAddressRequest orderAddressRequest = request.orderAddressRequest();
        Order order = Order.builder()
                .orderId(OrderNumberGenerator.generateOrderId())
                .user(user)
                .orderAddress(orderAddressRequest.toOrderAddress())
                .build();
        Order savedOrder = orderRepository.saveAndFlush(order);

        int itemsTotalPrice = orderItemAppender.saveOrderItems(request.orderItems(), savedOrder);
        int totalPrice = itemsTotalPrice + DELIVERY_FEE;
        if (totalPrice != request.amount()) {
            throw PriceMismatchException.EXCEPTION;
        }
        order.setTotalPrice(totalPrice);
        return CreateOrderResponse.from(order.getOrderId());
    }

    @Transactional
    public Order validateAndMarkPaymentRequested(String orderId, int amount) {
        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> OrderNotFoundException.EXCEPTION);

        User user = userReader.getCurrentUser();
        if (!order.getUser().equals(user)) {
            throw InvalidOrderInfoException.EXCEPTION;
        }

        if (order.getTotalPrice() != amount) {
            throw PriceMismatchException.EXCEPTION;
        }

        if (order.getOrderStatus() != OrderStatus.READY) {
            throw InvalidOrderInfoException.EXCEPTION;
        }

        order.paymentRequested();
        return order;
    }

    @Transactional
    public void processPaymentSuccess(Order order, PaymentsResponse response, PaymentIdempotency idempotency) {
        if (response.totalAmount() != order.getTotalPrice()) {
            throw PriceMismatchException.EXCEPTION;
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentType(PaymentType.CONFIRM)
                .paymentKey(response.paymentKey())
                .tossOrderId(response.orderId())
                .orderName(response.orderName())
                .provider(response.easyPayProvider())
                .method(PaymentMethod.from(response.method()))
                .totalAmount(response.totalAmount())
                .balanceAmount(response.balanceAmount())
                .requestedAt(response.requestedAt().toLocalDateTime())
                .approvedAt(response.approvedAtLocal())
                .lastTransactionKey(response.lastTransactionKey())
                .partialCancelable(response.isPartialCancelable())
                .cardInfo(new CardInfo(response.cardNumber(), response.cardIssuerCode()))
                .receiptUrl(response.receiptUrl())
                .virtualAccountSecret(response.secret())
                .virtualAccountInfo(response.virtualAccount() != null ? new VirtualAccountInfo(
                        response.virtualAccountNumber(),
                        response.virtualAccountBankCode(),
                        response.virtualAccountDueDateLocal(),
                        response.virtualAccountDepositorName()
                ) : null)
                .build();

        paymentRepository.save(payment);

        if (response.status() == PaymentsStatus.WAITING_FOR_DEPOSIT) {
            order.waitingForDeposit();
        } else {
            order.confirmPayment();
        }
        orderRepository.save(order);

        paymentIdempotencyManager.markSuccess(idempotency);
    }

    @Transactional
    public void processCancelSuccess(Long orderId, String cancelTransactionKey, String refundReason, PaymentIdempotency idempotency) {
        Order order = orderRepository.findByIdWithItemsAndIsDeletedFalse(orderId)
                .orElseThrow(() -> OrderNotFoundException.EXCEPTION);

        Payment cancelPayment = Payment.builder()
                .order(order)
                .paymentType(PaymentType.CANCEL)
                .totalAmount(order.getTotalPrice())
                .lastTransactionKey(cancelTransactionKey)
                .build();
        paymentRepository.save(cancelPayment);

        List<Refund> refunds = order.getOrderItems().stream()
                .map(orderItem -> Refund.builder()
                        .orderItem(orderItem)
                        .payment(cancelPayment)
                        .refundReason(refundReason)
                        .refundAmount(orderItem.getPrice() * orderItem.getQuantity())
                        .refundStatus(RefundStatus.COMPLETED)
                        .refundType(RefundType.CANCEL)
                        .cancelTransactionKey(cancelTransactionKey)
                        .build())
                .toList();
        refundAppender.saveAll(refunds);

        order.cancel();
        orderRepository.save(order);

        paymentIdempotencyManager.markSuccess(idempotency);
    }

    @Transactional
    public void processDepositCallback(VirtualAccountDepositCallbackRequest request) {
        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(request.orderId())
                .orElseThrow(() -> OrderNotFoundException.EXCEPTION);
        Payment confirmPayment = paymentReader.findConfirmByOrder(order);
        if (!request.secret().equals(confirmPayment.getVirtualAccountSecret())) {
            throw InvalidOrderInfoException.EXCEPTION;
        }
        if ("DONE".equals(request.status())) {
            Payment depositPayment = Payment.builder()
                    .order(order)
                    .paymentType(PaymentType.DEPOSIT)
                    .tossOrderId(request.orderId())
                    .lastTransactionKey(request.transactionKey())
                    .requestedAt(request.createdAt())
                    .build();
            paymentRepository.save(depositPayment);
            order.confirmPayment();
        }
    }

    @Transactional
    public void cancelOrder(Order order) {
        order.cancel();
    }

    @Transactional(readOnly = true)
    public PaymentCancelInfo findConfirmPaymentToCancel(Long orderId) {
        User currentUser = userReader.getCurrentUser();
        Order order = orderReader.findOrderById(orderId);
        if (!order.getUser().equals(currentUser)) {
            throw InvalidOrderInfoException.EXCEPTION;
        }
        if (!order.getOrderStatus().equals(OrderStatus.PAYMENT_CONFIRMED)) {
            throw CannotCancelOrderException.EXCEPTION;
        }
        Payment confirmPayment = paymentReader.findConfirmByOrder(order);
        return new PaymentCancelInfo(order.getId(), confirmPayment.getPaymentKey());
    }

    @Transactional(readOnly = true)
    public PageResponse<UserOrderInfo> getUserOrderList(Pageable pageable) {
        User user = userReader.getCurrentUser();
        Page<Order> orderPages = orderRepository.findOrderPagesByUser(pageable, user);
        return PageResponse.of(orderPages.map(UserOrderInfo::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<GetOrderItemUserCanReviewResponse> getUserCanReviewList(Pageable pageable) {
        User user = userReader.getCurrentUser();
        Page<OrderItem> orderItemPages = orderRepository.findUserOrderItemCanReview(pageable, user);
        Page<GetOrderItemUserCanReviewResponse> getOrderItemUserCanReviewResponses = orderItemPages.map(
                orderItem -> {
                    OrderOption orderOption = new OrderOption(orderItem.getColor(), orderItem.getQuantity());
                    return GetOrderItemUserCanReviewResponse.builder()
                            .productName(orderItem.getProduct().getName())
                            .orderItemId(orderItem.getId())
                            .orderOption(orderOption)
                            .build();
                }
        );
        return PageResponse.of(getOrderItemUserCanReviewResponses);
    }
}
