package com.mavis.admin.domains.admin.implement;

import com.mavis.admin.global.security.SecurityUtils;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.exception.AdminNotFoundException;
import com.mavis.domain.domains.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminReader {

    private final AdminRepository adminRepository;

    public Admin getCurrentAdmin() {
        Long adminId = SecurityUtils.getCurrentUserId();
        return adminRepository.findByIdAndIsDeletedFalse(adminId)
                .orElseThrow(() -> AdminNotFoundException.EXCEPTION);
    }
}
