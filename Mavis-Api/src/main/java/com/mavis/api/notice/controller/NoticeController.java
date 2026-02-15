package com.mavis.api.notice.controller;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.notice.dto.NoticeResponse;
import com.mavis.api.notice.dto.NoticeSummaryResponse;
import com.mavis.api.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/notice")
@Tag(name = "공지사항 API")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/{noticeId}")
    @Operation(summary = "공지사항 상세 조회")
    public NoticeResponse getNotice(@PathVariable Long noticeId) {
        return noticeService.getNotice(noticeId);
    }

    @GetMapping
    @Operation(summary = "공지사항 목록 조회")
    public PageResponse<NoticeSummaryResponse> getNotices(@ParameterObject Pageable pageable) {
        return noticeService.getNotices(pageable);
    }
}
