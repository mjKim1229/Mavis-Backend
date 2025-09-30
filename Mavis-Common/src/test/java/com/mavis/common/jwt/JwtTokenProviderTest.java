package com.mavis.common.jwt;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JwtTokenProviderTest {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void 액세스_토큰_파싱() {
        //given
        Long id = 1L;
        String accessToken = jwtTokenUtil.generateAccessToken(1L);

        //when
        Long payloadId = jwtTokenUtil.parseAccessToken(accessToken);

        //then
        Assertions.assertThat(id).isEqualTo(payloadId);
    }
}
