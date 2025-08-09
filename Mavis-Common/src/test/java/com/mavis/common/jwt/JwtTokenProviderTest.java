package com.mavis.common.jwt;

import com.mavis.common.jwt.JwtTokenProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.function.LongToIntFunction;

@SpringBootTest
public class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void 액세스_토큰_파싱() {
        //given
        Long id = 1L;
        String accessToken = jwtTokenProvider.generateAccessToken(1L);

        //when
        Long payloadId = jwtTokenProvider.parseAccessToken(accessToken);

        //then
        Assertions.assertThat(id).isEqualTo(payloadId);
    }
}
