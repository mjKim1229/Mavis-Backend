package com.mavis.admin.domains.notice.controller;

import com.mavis.admin.domains.notice.dto.CreateNoticeRequest;
import com.mavis.admin.domains.notice.dto.UpdateNoticeRequest;
import com.mavis.admin.domains.notice.service.AdminNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/notice")
@Tag(name = "관리자 공지사항 API")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @PostMapping
    @Operation(summary = "공지사항 생성")
    public void createNotice(@RequestBody CreateNoticeRequest request) {
        adminNoticeService.createNotice(request);
    }

    @PutMapping("/{noticeId}")
    @Operation(summary = "공지사항 수정")
    public void updateNotice(@PathVariable Long noticeId, @RequestBody UpdateNoticeRequest request) {
        adminNoticeService.updateNotice(noticeId, request);
    }

    @DeleteMapping("/{noticeId}")
    @Operation(summary = "공지사항 삭제")
    public void deleteNotice(@PathVariable Long noticeId) {
        adminNoticeService.deleteNotice(noticeId);
    }
}
