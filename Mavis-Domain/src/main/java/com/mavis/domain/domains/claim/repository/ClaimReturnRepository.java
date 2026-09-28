package com.mavis.domain.domains.claim.repository;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimReturnRepository extends JpaRepository<ClaimReturn, Long> {

    List<ClaimReturn> findByClaimIn(List<Claim> claims);
}
