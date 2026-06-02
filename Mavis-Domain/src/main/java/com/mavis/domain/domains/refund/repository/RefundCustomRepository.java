package com.mavis.domain.domains.refund.repository;

import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefundCustomRepository {

    Page<Refund> findRefundPages(RefundStatus refundStatus, Pageable pageable);
}
