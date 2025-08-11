package com.mavis.api.auth.implement;

import com.mavis.api.auth.domain.RefreshTokenEntity;
import com.mavis.api.auth.repository.RefreshTokenRepository;
import com.mavis.common.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenManager {

    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenEntity save(Long id, String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .id(id)
                .refreshToken(refreshToken)
                .ttl(jwtProperties.refreshExp())
                .build();
        return refreshTokenRepository.save(refreshTokenEntity);
    }

    public RefreshTokenEntity readByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken);
    }

    public void deleteById(Long id) {
        refreshTokenRepository.deleteById(id);
    }
}
