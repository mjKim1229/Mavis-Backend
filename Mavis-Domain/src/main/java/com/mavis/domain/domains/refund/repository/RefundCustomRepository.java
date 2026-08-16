package com.mavis.domain.domains.refund.repository;

import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.domain.RefundType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefundCustomRepository {

    Page<Refund> findRefundPages(RefundType refundType, RefundStatus refundStatus, Pageable pageable);
}
