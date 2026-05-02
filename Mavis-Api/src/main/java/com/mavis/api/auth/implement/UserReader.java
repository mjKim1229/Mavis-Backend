package com.mavis.api.auth.implement;

import com.mavis.api.global.security.SecurityUtils;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.exception.UserNotFoundException;
import com.mavis.domain.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.EXCEPTION);
    }
}
