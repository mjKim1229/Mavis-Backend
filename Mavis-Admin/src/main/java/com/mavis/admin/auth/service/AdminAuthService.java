package com.mavis.admin.auth.service;

import com.mavis.admin.auth.dto.AdminLoginRequest;
import com.mavis.common.jwt.JwtTokenProvider;
import com.mavis.domains.admin.domain.Admin;
import com.mavis.domains.admin.exception.AdminLoginException;
import com.mavis.domains.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public String adminLogin(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsernameAndIsDeletedFalse(request.username())
                .orElseThrow(() -> AdminLoginException.EXCEPTION);

        if (!admin.getPassword().equals(request.password())) {
            throw AdminLoginException.EXCEPTION;
        }

        return jwtTokenProvider.generateAccessToken(admin.getId());
    }
}
