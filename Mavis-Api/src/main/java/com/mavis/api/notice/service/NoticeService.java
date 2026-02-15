package com.mavis.api.notice.service;

import com.mavis.api.notice.dto.NoticeResponse;
import com.mavis.domain.domains.notice.domain.Notice;
import com.mavis.domain.domains.notice.exception.NoticeNotFoundException;
import com.mavis.domain.domains.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> NoticeNotFoundException.EXCEPTION);
        return NoticeResponse.of(notice);
    }
}
