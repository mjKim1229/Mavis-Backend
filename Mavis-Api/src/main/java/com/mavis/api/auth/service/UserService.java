package com.mavis.api.auth.service;

import com.mavis.domains.user.domain.SnsType;
import com.mavis.domains.user.domain.User;
import com.mavis.domains.user.repository.UserRepository;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
}
