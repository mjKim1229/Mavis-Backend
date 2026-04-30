package com.mavis.api.auth.service;

import com.mavis.api.auth.dto.PasswordChangeRequest;
import com.mavis.api.auth.dto.UserAddressRequest;
import com.mavis.api.auth.dto.UserDetailResponse;
import com.mavis.api.auth.dto.UserDetailUpdateRequest;
import com.mavis.api.auth.dto.UserLoginRequest;
import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.api.auth.dto.UserProfileResponse;
import com.mavis.api.auth.dto.UserSignUpRequest;
import com.mavis.api.auth.dto.UsernameCheckResponse;
import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.order.dto.OrderAddressResponse;
import com.mavis.common.dto.JwtPair;
import com.mavis.common.jwt.JwtTokenUtil;
import com.mavis.common.util.PhoneNormalizer;
import com.mavis.domain.domains.user.domain.Gender;
import com.mavis.domain.domains.user.domain.MarketingAgreement;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.exception.InvalidPasswordException;
import com.mavis.domain.domains.user.exception.SnsUserCannotChangePasswordException;
import com.mavis.domain.domains.user.exception.UserNotFoundException;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoUserInfoResponse;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserReader userReader;

    private static final DateTimeFormatter NAVER_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM-dd");
    private static final DateTimeFormatter KAKAO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional
    public Long upsertNaverUser(NaverProfile profile, String naverRefreshToken, boolean isEmailAgreed, boolean isSmsAgreed) {
        return userRepository.findBySnsTypeAndSnsIdAndIsDeletedFalse(SnsType.NAVER, profile.id())
                .orElseGet(() -> saveNaverUser(profile, naverRefreshToken, isEmailAgreed, isSmsAgreed))
                .getId();
    }

    private User saveNaverUser(NaverProfile profile, String naverRefreshToken, boolean isEmailAgreed, boolean isSmsAgreed) {
        User user = User.builder()
                .snsId(profile.id())
                .nickname(profile.nickname())
                .name(profile.name())
                .email(profile.email())
                .gender(Gender.fromCode(profile.gender()))
                .birthDay(toLocalDate(profile.birthyear() + profile.birthday(), NAVER_DATE_TIME_FORMATTER))
                .phoneNumber(PhoneNormalizer.normalize(profile.mobileE164()))
                .age(profile.age())
                .profileImage(profile.profileImage())
                .snsType(SnsType.NAVER)
                .naverRefreshToken(naverRefreshToken)
                .marketingAgreement(MarketingAgreement.of(isEmailAgreed, isSmsAgreed))
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public Long upsertKakaouser(KakaoUserInfoResponse kakaoUserInfoResponse, boolean isEmailAgreed, boolean isSmsAgreed) {
        String kakaoSnsId = String.valueOf(kakaoUserInfoResponse.id());
        return userRepository.findBySnsTypeAndSnsIdAndIsDeletedFalse(SnsType.KAKAO, kakaoSnsId)
                .orElseGet(() -> saveKakaoUser(kakaoUserInfoResponse, isEmailAgreed, isSmsAgreed))
                .getId();
    }

    private User saveKakaoUser(KakaoUserInfoResponse kakaoUserInfoResponse, boolean isEmailAgreed, boolean isSmsAgreed) {
        String snsId = String.valueOf(kakaoUserInfoResponse.id());
        KakaoUserInfoResponse.KakaoAccount account = kakaoUserInfoResponse.kakaoAccount();

        User user = User.builder()
                .snsId(snsId)
                .email(account.email())
                .name(account.name())
                .gender(Gender.fromCode(account.gender()))
                .birthDay(toLocalDate(account.birthyear() + account.birthday(), KAKAO_DATE_TIME_FORMATTER))
                .phoneNumber(PhoneNormalizer.normalize(account.phoneNumber()))
                .nickname(account.profile() != null ? account.profile().nickname() : null)
                .profileImage(account.profile() != null ? account.profile().image() : null)
                .snsType(SnsType.KAKAO)
                .marketingAgreement(MarketingAgreement.of(isEmailAgreed, isSmsAgreed))
                .build();
        return userRepository.save(user);
    }

    private static LocalDate toLocalDate(String dateString, DateTimeFormatter formatter) {
        return LocalDate.parse(dateString, formatter);
    }

    @Transactional
    public void signUp(UserSignUpRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = request.toEntity(encodedPassword);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserOauthResponse login(UserLoginRequest request) {
        User user = userRepository.findByUsernameAndIsDeletedFalse(request.username())
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw UserNotFoundException.EXCEPTION;
        }
        String accessToken = jwtTokenUtil.generateAccessToken(user.getId(), "USER");
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId());
        return new UserOauthResponse(
                user.getId(),
                new JwtPair(accessToken, refreshToken)
        );
    }

    public UserOauthResponse tokenRefresh(String refreshToken) {
        Long id = jwtTokenUtil.parseRefreshToken(refreshToken);
        String accessToken = jwtTokenUtil.generateAccessToken(id, "USER");
        refreshToken = jwtTokenUtil.generateRefreshToken(id);
        return new UserOauthResponse(
                id,
                new JwtPair(accessToken, refreshToken)
        );
    }

    @Transactional
    public void updateAddress(UserAddressRequest request) {
        User user = userReader.getCurrentUser();
        user.updateAddress(request.toDefaultDeliveryAddress());
    }

    @Transactional
    public void createAddress(UserAddressRequest request) {
        User user = userReader.getCurrentUser();
        user.updateAddress(request.toDefaultDeliveryAddress());
    }

    @Transactional(readOnly = true)
    public OrderAddressResponse getUserAddress() {
        User user = userReader.getCurrentUser();
        return OrderAddressResponse.from(user);
    }

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsernameAndIsDeletedFalse(username);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile() {
        User user = userReader.getCurrentUser();
        return UserProfileResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail() {
        User user = userReader.getCurrentUser();
        return UserDetailResponse.from(user);
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(String password) {
        User user = userReader.getCurrentUser();
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    public void changePassword(String currentPassword, String newPassword) {
        User user = userReader.getCurrentUser();
        if (user.getSnsType() != SnsType.MANUAL) {
            throw SnsUserCannotChangePasswordException.EXCEPTION;
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw InvalidPasswordException.EXCEPTION;
        }
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedNewPassword);
    }

    @Transactional
    public void updateUserDetail(UserDetailUpdateRequest request) {
        User user = userReader.getCurrentUser();
        user.updateProfile(
                request.nickname(),
                request.name(),
                request.gender(),
                request.birthDay(),
                request.phoneNumber()
        );
    }
}
