package com.mavis.admin.domains.notice.service;

import com.mavis.admin.domains.notice.dto.CreateNoticeRequest;
import com.mavis.domain.domains.notice.domain.Notice;
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
}
