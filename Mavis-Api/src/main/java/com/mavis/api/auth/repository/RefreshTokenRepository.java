package com.mavis.api.auth.repository;

import com.mavis.api.auth.domain.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, Long> {
    RefreshTokenEntity findByRefreshToken(String refreshToken);
}
