package com.mavis.common.jwt;

import com.mavis.common.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static com.mavis.common.consts.MavisStatic.*;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private Jws<Claims> getJws(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long id) {
        final Key encodedKey = getSecretKey();
        final Date issuedAt = new Date();
        final Date accessTokenExpiresIn =
                new Date(issuedAt.getTime() + jwtProperties.accessExp() * MILLI_TO_SECOND);

        return Jwts.builder()
                .issuer(TOKEN_ISSUER)
                .issuedAt(issuedAt)
                .subject(id.toString())
                .claim(TOKEN_TYPE, ACCESS_TOKEN)
                .expiration(accessTokenExpiresIn)
                .signWith(encodedKey)
                .compact();
    }

    public String generateRefreshToken(Long id) {
        final Key encodedKey = getSecretKey();
        final Date issuedAt = new Date();
        final Date refreshTokenExpiresIn =
                new Date(issuedAt.getTime() + jwtProperties.refreshExp() * MILLI_TO_SECOND);

        return Jwts.builder()
                .issuer(TOKEN_ISSUER)
                .issuedAt(issuedAt)
                .subject(id.toString())
                .claim(TOKEN_TYPE, REFRESH_TOKEN)
                .expiration(refreshTokenExpiresIn)
                .signWith(encodedKey)
                .compact();
    }

    private boolean isAccessToken(String token) {
        Claims payload = getJws(token).getPayload();
        return payload.get(TOKEN_TYPE).equals(ACCESS_TOKEN);
    }

    private boolean isRefreshToken(String token) {
        Jws<Claims> jws = getJws(token);
        return jws.getPayload().get(TOKEN_TYPE).equals(REFRESH_TOKEN);
    }

    public Long parseAccessToken(String token) {
        if (isAccessToken(token)) {
            Claims payload = getJws(token).getPayload();
            String id = payload.getSubject();
            return Long.valueOf(id);
        }
        throw new RuntimeException();
    }
}
