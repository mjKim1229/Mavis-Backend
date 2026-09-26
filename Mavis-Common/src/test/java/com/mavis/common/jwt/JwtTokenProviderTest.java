package com.mavis.common.jwt;

import com.mavis.common.exception.InvalidTokenException;
import com.mavis.common.properties.JwtProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    // 스프링 컨텍스트 없이 검증 — 프로퍼티는 테스트용 값으로 직접 주입한다
    private final JwtProperties jwtProperties =
            new JwtProperties("test-secret-key-for-jwt-token-should-be-long-enough-256bit", 3600L, 86400L);
    private final JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(jwtProperties);

    @Test
    void 액세스_토큰_파싱() {
        Long id = 1L;

        String accessToken = jwtTokenUtil.generateAccessToken(id, "USER");
        Long payloadId = jwtTokenUtil.parseAccessToken(accessToken);

        assertThat(payloadId).isEqualTo(id);
        assertThat(jwtTokenUtil.getRoleFromToken(accessToken)).isEqualTo("USER");
    }

    @Test
    void 리프레시_토큰_파싱() {
        Long id = 2L;

        String refreshToken = jwtTokenUtil.generateRefreshToken(id);

        assertThat(jwtTokenUtil.isRefreshToken(refreshToken)).isTrue();
        assertThat(jwtTokenUtil.parseRefreshToken(refreshToken)).isEqualTo(id);
    }

    @Test
    void 리프레시_토큰을_액세스_토큰으로_파싱하면_InvalidTokenException() {
        String refreshToken = jwtTokenUtil.generateRefreshToken(3L);

        assertThatThrownBy(() -> jwtTokenUtil.parseAccessToken(refreshToken))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void 위조된_토큰은_InvalidTokenException() {
        String accessToken = jwtTokenUtil.generateAccessToken(4L, "USER");
        String tampered = accessToken.substring(0, accessToken.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtTokenUtil.parseAccessToken(tampered))
                .isInstanceOf(InvalidTokenException.class);
    }
}
