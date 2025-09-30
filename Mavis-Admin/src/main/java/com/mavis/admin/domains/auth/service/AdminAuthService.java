package com.mavis.admin.domains.auth.service;

import com.mavis.admin.domains.auth.dto.AdminLoginRequest;
import com.mavis.admin.domains.auth.dto.AdminLoginResponse;
import com.mavis.common.jwt.JwtTokenUtil;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.exception.AdminLoginException;
import com.mavis.domain.domains.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public AdminLoginResponse adminLogin(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsernameAndIsDeletedFalse(request.username())
                .orElseThrow(() -> AdminLoginException.EXCEPTION);

        if (!admin.getPassword().equals(request.password())) {
            throw AdminLoginException.EXCEPTION;
        }

        String accessToken = jwtTokenUtil.generateAccessToken(admin.getId());
        return new AdminLoginResponse(accessToken);
    }
}
