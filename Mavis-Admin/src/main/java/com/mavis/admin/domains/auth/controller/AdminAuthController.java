package com.mavis.admin.domains.auth.controller;

import com.mavis.admin.domains.auth.dto.*;
import com.mavis.admin.domains.auth.service.AdminAuthService;
import com.mavis.admin.domains.auth.service.AdminAuthVerificationService;
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
    private final AdminAuthVerificationService adminAuthVerificationService;

    @PostMapping
    @Operation(summary = "관리자 로그인")
    public AdminLoginResponse adminLogin(@RequestBody AdminLoginRequest request) {
        return adminAuthService.adminLogin(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "관리자 토큰 재발급")
    public AdminLoginResponse adminTokenRefresh(@RequestHeader("refreshToken") String refreshToken) {
        return adminAuthService.adminTokenRefresh(refreshToken);
    }

    @PostMapping("/password/reset")
    @Operation(summary = "비밀번호 재설정 메일 발송")
    public void sendPasswordResetEmail(@RequestBody AdminPasswordResetEmailRequest request) {
        adminAuthVerificationService.sendPasswordResetEmail(request);
    }

    @PutMapping("/password/reset")
    @Operation(summary = "비밀번호 재설정 확인")
    public void confirmPasswordReset(@RequestBody AdminPasswordResetConfirmRequest request) {
        adminAuthVerificationService.confirmPasswordReset(request);
    }

    @PutMapping("/password")
    @Operation(summary = "비밀번호 변경 (로그인 상태)")
    public void changePassword(@RequestBody AdminPasswordChangeRequest request) {
        adminAuthVerificationService.changePassword(request);
    }
}
