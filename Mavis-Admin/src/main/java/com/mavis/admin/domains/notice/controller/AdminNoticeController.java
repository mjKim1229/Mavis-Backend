package com.mavis.admin.domains.notice.controller;

import com.mavis.admin.domains.notice.dto.CreateNoticeRequest;
import com.mavis.admin.domains.notice.service.AdminNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
