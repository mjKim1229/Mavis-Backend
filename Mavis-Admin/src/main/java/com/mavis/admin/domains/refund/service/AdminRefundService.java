package com.mavis.admin.domains.refund.service;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.refund.dto.GetAdminCanceledRefundResponse;
import com.mavis.admin.domains.refund.dto.GetAdminRefundResponse;
import com.mavis.admin.domains.refund.dto.GetApprovePreviewResponse;
import com.mavis.admin.domains.refund.dto.RefundValidateInfo;
import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimItem;
import com.mavis.domain.domains.claim.domain.ClaimReturn;
import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.claim.domain.ClaimType;
import com.mavis.domain.domains.claim.domain.ReturnDecision;
import com.mavis.domain.domains.claim.domain.ReturnRefundPlan;
import com.mavis.domain.domains.claim.implement.ClaimReader;
import com.mavis.domain.domains.claim.repository.ClaimRepository;
import com.mavis.domain.domains.claim.repository.ClaimReturnRepository;
import com.mavis.domain.domains.order.domain.CardInfo;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentMethod;
import com.mavis.domain.domains.order.domain.PaymentType;
import com.mavis.domain.domains.order.implement.PaymentReader;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsCancels;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AdminRefundService {

    private final ClaimRepository claimRepository;
    private final ClaimReturnRepository claimReturnRepository;
    private final ClaimReader claimReader;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentReader paymentReader;

    @Transactional(readOnly = true)
    public PageResponse<GetAdminRefundResponse> getRefundList(Pageable pageable, ClaimStatus claimStatus) {
        Page<Claim> claimPages = claimRepository.findClaimPages(ClaimType.RETURN, claimStatus, pageable);
        List<Claim> claims = claimPages.getContent();

        Map<Long, ClaimReturn> claimReturnMap = claimReturnRepository.findByClaimIn(claims).stream()
                .collect(Collectors.toMap(claimReturn -> claimReturn.getClaim().getId(), claimReturn -> claimReturn));
        Map<Long, Refund> refundMap = refundRepository.findByClaimInWithPayment(claims).stream()
                .collect(Collectors.toMap(refund -> refund.getClaim().getId(), refund -> refund));
        List<Order> orders = claims.stream()
                .map(Claim::getOrder)
                .distinct()
                .toList();
        Set<Long> settledOrderIds = claimReader.findOrderIdsWithCompletedReturn(orders);

        return PageResponse.of(claimPages.map(claim -> {
            boolean orderShippingSettled = settledOrderIds.contains(claim.getOrder().getId());
            return GetAdminRefundResponse.from(
                    claim, claimReturnMap.get(claim.getId()), refundMap.get(claim.getId()), orderShippingSettled);
        }));
    }

    @Transactional(readOnly = true)
    public PageResponse<GetAdminCanceledRefundResponse> getCanceledRefundList(Pageable pageable) {
        Page<ClaimItem> claimItemPages = claimRepository.findClaimItemPages(ClaimType.CANCEL, pageable);
        List<Claim> claims = claimItemPages.getContent().stream()
                .map(ClaimItem::getClaim)
                .distinct()
                .toList();

        Map<Long, Refund> refundMap = refundRepository.findByClaimInWithPayment(claims).stream()
                .collect(Collectors.toMap(refund -> refund.getClaim().getId(), refund -> refund));

        return PageResponse.of(claimItemPages.map(claimItem -> {
            Claim claim = claimItem.getClaim();
            Refund refund = refundMap.get(claim.getId());
            return GetAdminCanceledRefundResponse.from(claimItem, refund);
        }));
    }

    @Transactional(readOnly = true)
    public RefundValidateInfo validateForApproval(Long claimId, ReturnDecision decision, Integer amount) {
        Claim claim = claimReader.findByIdWithOrder(claimId);
        if (claim.getClaimType() != ClaimType.RETURN || claim.getClaimStatus() != ClaimStatus.REQUESTED) {
            throw CannotRefundException.EXCEPTION;
        }
        Order order = claim.getOrder();
        Payment confirmPayment = paymentReader.findConfirmByOrder(order);
        boolean orderShippingSettled = claimReader.hasCompletedReturn(order);
        int canceledAmount = paymentReader.sumCanceledAmount(order);
        int maxRefundable = confirmPayment.getTotalAmount() - canceledAmount;
        ReturnRefundPlan plan = claim.planReturnRefund(decision, amount, orderShippingSettled, maxRefundable);
        return RefundValidateInfo.from(claim, confirmPayment, maxRefundable, plan);
    }

    @Transactional(readOnly = true)
    public GetApprovePreviewResponse previewApproval(Long claimId, ReturnDecision decision, Integer amount) {
        RefundValidateInfo info = validateForApproval(claimId, decision, amount);
        return GetApprovePreviewResponse.from(info);
    }

    @Transactional
    public void approveAndComplete(RefundValidateInfo info, PaymentsResponse response, PaymentsCancels cancelEntry) {
        Claim claim = claimReader.findByIdWithOrder(info.claimId());
        Order order = claim.getOrder();

        Payment cancelPayment = Payment.builder()
                .order(order)
                .paymentType(PaymentType.CANCEL)
                .paymentKey(response.paymentKey())
                .tossOrderId(response.orderId())
                .orderName(response.orderName())
                .provider(response.easyPayProvider())
                .method(PaymentMethod.from(response.method()))
                .totalAmount(cancelEntry.cancelAmount())
                .balanceAmount(response.balanceAmount())
                .requestedAt(response.requestedAt().toLocalDateTime())
                .canceledAt(cancelEntry.canceledAt().toLocalDateTime())
                .lastTransactionKey(cancelEntry.transactionKey())
                .partialCancelable(response.isPartialCancelable())
                .cardInfo(new CardInfo(response.cardNumber(), response.cardIssuerCode()))
                .receiptUrl(response.receiptUrl())
                .cancelAmount(cancelEntry.cancelAmount())
                .cancelReason(cancelEntry.cancelReason())
                .build();

        Payment saved = paymentRepository.save(cancelPayment);
        Refund refund = Refund.forReturn(claim, saved, info.plan());
        refundRepository.save(refund);
        claim.complete(info.plan().faultParty());
    }

    @Transactional
    public void rejectRefund(Long claimId) {
        Claim claim = claimReader.findByIdWithOrder(claimId);
        claim.reject();
    }
}
