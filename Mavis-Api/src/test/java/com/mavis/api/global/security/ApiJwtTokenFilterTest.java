package com.mavis.api.global.security;

import com.mavis.common.exception.InvalidTokenException;
import com.mavis.common.jwt.JwtTokenUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.mavis.common.consts.MavisStatic.AUTH_HEADER;
import static com.mavis.common.consts.MavisStatic.BEARER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ApiJwtTokenFilterTest {

    @InjectMocks
    private JwtTokenFilter jwtTokenFilter;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void USER_role_토큰은_인증에_성공하고_ROLE_USER_권한을_가진다() throws Exception {
        // given
        String token = "user.token.value";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTH_HEADER, BEARER + token);

        given(jwtTokenUtil.parseAccessToken(token)).willReturn(1L);
        given(jwtTokenUtil.getRoleFromToken(token)).willReturn("USER");

        // when
        jwtTokenFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    void ADMIN_role_토큰은_API_필터에서_InvalidTokenException을_던진다() {
        // given
        String token = "admin.token.value";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTH_HEADER, BEARER + token);

        given(jwtTokenUtil.parseAccessToken(token)).willReturn(1L);
        given(jwtTokenUtil.getRoleFromToken(token)).willReturn("ADMIN");

        // when & then
        assertThatThrownBy(() ->
                jwtTokenFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain())
        ).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void role이_없는_토큰은_API_필터에서_InvalidTokenException을_던진다() {
        // given
        String token = "no.role.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTH_HEADER, BEARER + token);

        given(jwtTokenUtil.parseAccessToken(token)).willReturn(1L);
        given(jwtTokenUtil.getRoleFromToken(token)).willReturn(null);

        // when & then
        assertThatThrownBy(() ->
                jwtTokenFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain())
        ).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void 토큰이_없으면_SecurityContext에_인증이_저장되지_않는다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();

        // when
        jwtTokenFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
