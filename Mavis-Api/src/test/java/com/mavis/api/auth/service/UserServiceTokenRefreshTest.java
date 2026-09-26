package com.mavis.api.auth.service;

import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.common.jwt.JwtTokenUtil;
import com.mavis.domain.domains.user.exception.UserNotFoundException;
import com.mavis.domain.domains.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTokenRefreshTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void 활성_사용자는_토큰을_재발급받는다() {
        given(jwtTokenUtil.parseRefreshToken("refresh-token", "USER")).willReturn(1L);
        given(userRepository.existsByIdAndIsDeletedFalse(1L)).willReturn(true);
        given(jwtTokenUtil.generateAccessToken(1L, "USER")).willReturn("new-access");
        given(jwtTokenUtil.generateRefreshToken(1L, "USER")).willReturn("new-refresh");

        UserOauthResponse response = userService.tokenRefresh("refresh-token");

        assertThat(response.jwtPair().accessToken()).isEqualTo("new-access");
        assertThat(response.jwtPair().refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void 탈퇴했거나_존재하지_않는_사용자는_재발급받을_수_없다() {
        given(jwtTokenUtil.parseRefreshToken("refresh-token", "USER")).willReturn(1L);
        given(userRepository.existsByIdAndIsDeletedFalse(1L)).willReturn(false);

        assertThatThrownBy(() -> userService.tokenRefresh("refresh-token"))
                .isInstanceOf(UserNotFoundException.class);

        then(jwtTokenUtil).should(never()).generateAccessToken(anyLong(), any());
        then(jwtTokenUtil).should(never()).generateRefreshToken(anyLong(), any());
    }
}
