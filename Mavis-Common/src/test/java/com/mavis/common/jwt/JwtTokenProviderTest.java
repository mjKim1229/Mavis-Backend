package com.mavis.common.jwt;

import com.mavis.common.exception.InvalidTokenException;
import com.mavis.common.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.mavis.common.consts.MavisStatic.REFRESH_TOKEN;
import static com.mavis.common.consts.MavisStatic.TOKEN_ISSUER;
import static com.mavis.common.consts.MavisStatic.TOKEN_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-for-jwt-token-should-be-long-enough-256bit";
    private final JwtProperties jwtProperties = new JwtProperties(SECRET, 3600L, 86400L);
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
    void 리프레시_토큰은_같은_역할로_파싱하면_id를_반환한다() {
        Long id = 2L;

        String refreshToken = jwtTokenUtil.generateRefreshToken(id, "USER");

        assertThat(jwtTokenUtil.isRefreshToken(refreshToken)).isTrue();
        assertThat(jwtTokenUtil.parseRefreshToken(refreshToken, "USER")).isEqualTo(id);
    }

    @Test
    void 사용자_리프레시_토큰으로_어드민_재발급을_시도하면_InvalidTokenException() {
        String userRefreshToken = jwtTokenUtil.generateRefreshToken(1L, "USER");

        assertThatThrownBy(() -> jwtTokenUtil.parseRefreshToken(userRefreshToken, "ADMIN"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void 어드민_리프레시_토큰으로_사용자_재발급을_시도하면_InvalidTokenException() {
        String adminRefreshToken = jwtTokenUtil.generateRefreshToken(1L, "ADMIN");

        assertThatThrownBy(() -> jwtTokenUtil.parseRefreshToken(adminRefreshToken, "USER"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void 역할이_없는_옛_형식_리프레시_토큰은_InvalidTokenException() {
        String legacyRefreshToken = Jwts.builder()
                .issuer(TOKEN_ISSUER)
                .issuedAt(new Date())
                .subject("1")
                .claim(TOKEN_TYPE, REFRESH_TOKEN)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThatThrownBy(() -> jwtTokenUtil.parseRefreshToken(legacyRefreshToken, "USER"))
                .isInstanceOf(InvalidTokenException.class);
        assertThatThrownBy(() -> jwtTokenUtil.parseRefreshToken(legacyRefreshToken, "ADMIN"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void 리프레시_토큰을_액세스_토큰으로_파싱하면_InvalidTokenException() {
        String refreshToken = jwtTokenUtil.generateRefreshToken(3L, "USER");

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
