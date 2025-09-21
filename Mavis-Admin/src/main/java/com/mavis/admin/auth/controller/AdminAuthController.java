package com.mavis.admin.auth.controller;

import com.mavis.admin.auth.dto.AdminLoginRequest;
import com.mavis.admin.auth.dto.AdminLoginResponse;
import com.mavis.admin.auth.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping
    public AdminLoginResponse adminLogin(@RequestBody AdminLoginRequest request) {
        return adminAuthService.adminLogin(request);
    }
}
