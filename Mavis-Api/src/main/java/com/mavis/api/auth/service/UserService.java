package com.mavis.api.auth.service;

import com.mavis.api.auth.dto.UserLoginRequest;
import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.common.dto.JwtPair;
import com.mavis.common.jwt.JwtTokenUtil;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.exception.UserNotFoundException;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public Long saveNaverUser(NaverProfile profile) {
        User user = User.builder()
                .snsId(profile.id())
                .name(profile.name())
                .nickname(profile.nickname())
                .email(profile.email())
                .gender(profile.gender())
                .birthDay(profile.birthyear() + "-" + profile.birthday())
                .snsType(SnsType.NAVER)
                .build();
        return userRepository.save(user).getId();
    }

    public UserOauthResponse login(UserLoginRequest request) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(request.username())
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
        if (!user.getPassword().equals(request.password())) {
            throw UserNotFoundException.EXCEPTION;
        }
        String accessToken = jwtTokenUtil.generateAccessToken(user.getId());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId());
        return new UserOauthResponse(
                user.getId(),
                new JwtPair(accessToken, refreshToken)
        );
    }
}
