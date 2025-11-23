package com.mavis.api.auth.service;

import com.mavis.api.auth.dto.UserLoginRequest;
import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.api.auth.dto.UserSignUpRequest;
import com.mavis.api.auth.implement.UserReader;
import com.mavis.common.dto.JwtPair;
import com.mavis.common.jwt.JwtTokenUtil;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.exception.UserNotFoundException;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class UserService {

    private final UserReader userReader;
    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public Long upsertNaverUser(NaverProfile profile) {
        return userRepository.findBySnsTypeAndSnsIdAndIsDeletedFalse(SnsType.NAVER, profile.id())
                .orElseGet(() -> saveNaverUser(profile)).getId();
    }

    private User saveNaverUser(NaverProfile profile) {
        User user = User.builder()
                .snsId(profile.id())
                .name(profile.name())
                .nickname(profile.nickname())
                .email(profile.email())
                .gender(profile.gender())
                .birthDay(toLocalDate(profile.birthyear(), profile.birthday()))
                .snsType(SnsType.NAVER)
                .build();
        return userRepository.save(user);
    }

    private static LocalDate toLocalDate(String birthYear, String birthday) {
        String fullDate = birthYear + "-" + birthday;
        return LocalDate.parse(fullDate, DATE_TIME_FORMATTER);
    }

    @Transactional
    public void deleteKakaoUser(User user) {
        user.withDraw();
    }

    @Transactional
    public void signUp(UserSignUpRequest request) {
        User user = request.toEntity();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public void withDraw() {
        User user = userReader.getCurrentUser();
        user.withDraw();
    }
}
