package com.mavis.domain.domains.claim.repository;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.claim.domain.ClaimType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClaimRepository extends JpaRepository<Claim, Long>, ClaimCustomRepository {

    long countByClaimTypeAndClaimStatus(ClaimType claimType, ClaimStatus claimStatus);

    @Query("SELECT c FROM Claim c JOIN FETCH c.order WHERE c.id = :id")
    Optional<Claim> findByIdWithOrder(@Param("id") Long id);
}
