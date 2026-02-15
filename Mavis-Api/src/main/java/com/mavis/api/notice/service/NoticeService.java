package com.mavis.api.notice.service;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.notice.dto.NoticeResponse;
import com.mavis.api.notice.dto.NoticeSummaryResponse;
import com.mavis.domain.domains.notice.domain.Notice;
import com.mavis.domain.domains.notice.exception.NoticeNotFoundException;
import com.mavis.domain.domains.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findByIdAndIsDeletedFalse(noticeId)
                .orElseThrow(() -> NoticeNotFoundException.EXCEPTION);
        return NoticeResponse.of(notice);
    }

    @Transactional(readOnly = true)
    public PageResponse<NoticeSummaryResponse> getNotices(Pageable pageable) {
        Page<NoticeSummaryResponse> noticePages = noticeRepository.findAllByIsDeletedFalse(pageable)
                .map(NoticeSummaryResponse::of);
        return PageResponse.of(noticePages);
    }
}
