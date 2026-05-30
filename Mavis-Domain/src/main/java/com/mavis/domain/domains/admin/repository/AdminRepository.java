package com.mavis.domain.domains.admin.repository;

import com.mavis.domain.domains.admin.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsernameAndIsDeletedFalse(String username);
    Optional<Admin> findByIdAndIsDeletedFalse(Long id);
    Optional<Admin> findByEmailAndIsDeletedFalse(String email);
    boolean existsByEmailAndIsDeletedFalse(String email);
}
