package com.mavis.admin.domains.notice.service;

import com.mavis.admin.domains.notice.dto.CreateNoticeRequest;
import com.mavis.admin.domains.notice.dto.UpdateNoticeRequest;
import com.mavis.domain.domains.notice.domain.Notice;
import com.mavis.domain.domains.notice.exception.NoticeNotFoundException;
import com.mavis.domain.domains.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public void createNotice(CreateNoticeRequest request) {
        Notice notice = request.toEntity();
        noticeRepository.save(notice);
    }

    @Transactional
    public void updateNotice(Long noticeId, UpdateNoticeRequest request) {
        Notice notice = noticeRepository.findByIdAndIsDeletedFalse(noticeId)
                .orElseThrow(() -> NoticeNotFoundException.EXCEPTION);
        notice.update(request.category(), request.title(), request.content());
    }

    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findByIdAndIsDeletedFalse(noticeId)
                .orElseThrow(() -> NoticeNotFoundException.EXCEPTION);
        notice.delete();
    }
}
