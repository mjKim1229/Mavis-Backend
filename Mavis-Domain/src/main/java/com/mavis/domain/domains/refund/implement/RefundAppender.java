package com.mavis.domain.domains.refund.implement;

import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RefundAppender {

    private final RefundRepository refundRepository;

    public Refund save(Refund refund) {
        return refundRepository.save(refund);
    }

    public List<Refund> saveAll(List<Refund> refunds) {
        return refundRepository.saveAll(refunds);
    }
}
