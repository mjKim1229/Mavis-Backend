package com.mavis.admin.auth.controller;

import com.mavis.admin.auth.dto.AdminLoginRequest;
import com.mavis.admin.auth.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @GetMapping
    public String adminLogin(AdminLoginRequest request) {
        return adminAuthService.adminLogin(request);
    }
}
