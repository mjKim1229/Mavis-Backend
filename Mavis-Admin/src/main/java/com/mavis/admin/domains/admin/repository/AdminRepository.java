package com.mavis.admin.domains.admin.repository;

import com.mavis.admin.domains.admin.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsernameAndIsDeletedFalse(String username);
}
