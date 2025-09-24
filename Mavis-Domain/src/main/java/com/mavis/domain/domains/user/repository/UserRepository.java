package com.mavis.domain.domains.user.repository;

import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    public Optional<User> findByUsernameAndIsDeletedFalse(String username);
}
