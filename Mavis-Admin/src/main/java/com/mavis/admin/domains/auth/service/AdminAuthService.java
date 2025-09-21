package com.mavis.admin.domains.auth.service;

import com.mavis.admin.domains.auth.dto.AdminLoginRequest;
import com.mavis.admin.domains.auth.dto.AdminLoginResponse;
import com.mavis.common.jwt.JwtTokenProvider;
import com.mavis.admin.domains.admin.domain.Admin;
import com.mavis.admin.domains.admin.exception.AdminLoginException;
import com.mavis.admin.domains.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminLoginResponse adminLogin(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsernameAndIsDeletedFalse(request.username())
                .orElseThrow(() -> AdminLoginException.EXCEPTION);

        if (!admin.getPassword().equals(request.password())) {
            throw AdminLoginException.EXCEPTION;
        }

        String accessToken = jwtTokenProvider.generateAccessToken(admin.getId());
        return new AdminLoginResponse(accessToken);
    }
}
