package com.mavis.admin.domains.refund.service;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.refund.dto.GetAdminRefundResponse;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import com.mavis.domain.domains.refund.implement.RefundReader;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRefundService {

    private final RefundRepository refundRepository;
    private final RefundReader refundReader;

    @Transactional(readOnly = true)
    public PageResponse<GetAdminRefundResponse> getRefundList(Pageable pageable, RefundStatus refundStatus) {
        Page<Refund> refundPages = refundStatus == null
                ? refundRepository.findAll(pageable)
                : refundRepository.findByRefundStatus(refundStatus, pageable);
        return PageResponse.of(refundPages.map(GetAdminRefundResponse::from));
    }

    @Transactional
    public Refund approveRefund(Long refundId) {
        Refund refund = refundReader.findByIdWithOrderItemAndOrder(refundId);
        if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
            throw CannotRefundException.EXCEPTION;
        }
        refund.approve();
        return refund;
    }

    @Transactional
    public void rejectRefund(Long refundId) {
        Refund refund = refundReader.findById(refundId);
        if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
            throw CannotRefundException.EXCEPTION;
        }
        refund.reject();
    }

    @Transactional
    public void completeRefund(Refund refund, String cancelTransactionKey) {
        refund.complete(cancelTransactionKey);
    }
}
