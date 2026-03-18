package com.mavis.admin.domains.auth.controller;

import com.mavis.admin.domains.auth.dto.AdminLoginRequest;
import com.mavis.admin.domains.auth.dto.AdminLoginResponse;
import com.mavis.admin.domains.auth.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/admin/auth")
@Tag(name = "관리자 인증 API")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping
    @Operation(summary = "관리자 로그인")
    public AdminLoginResponse adminLogin(@RequestBody AdminLoginRequest request) {
        return adminAuthService.adminLogin(request);
    }
}
