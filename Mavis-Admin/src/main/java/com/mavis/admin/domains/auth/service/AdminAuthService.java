package com.mavis.admin.domains.auth.service;

import com.mavis.admin.domains.auth.dto.AdminLoginRequest;
import com.mavis.admin.domains.auth.dto.AdminLoginResponse;
import com.mavis.common.dto.JwtPair;
import com.mavis.common.jwt.JwtTokenUtil;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.exception.AdminLoginException;
import com.mavis.domain.domains.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    public AdminLoginResponse adminLogin(AdminLoginRequest request) {
        Admin admin = adminRepository.findByUsernameAndIsDeletedFalse(request.username())
                .orElseThrow(() -> AdminLoginException.EXCEPTION);

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw AdminLoginException.EXCEPTION;
        }

        String accessToken = jwtTokenUtil.generateAccessToken(admin.getId(), "ADMIN");
        String refreshToken = jwtTokenUtil.generateRefreshToken(admin.getId());
        return new AdminLoginResponse(new JwtPair(accessToken, refreshToken));
    }

    public AdminLoginResponse adminTokenRefresh(String refreshToken) {
        Long adminId = jwtTokenUtil.parseRefreshToken(refreshToken);
        String accessToken = jwtTokenUtil.generateAccessToken(adminId, "ADMIN");
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(adminId);
        return new AdminLoginResponse(new JwtPair(accessToken, newRefreshToken));
    }
}
