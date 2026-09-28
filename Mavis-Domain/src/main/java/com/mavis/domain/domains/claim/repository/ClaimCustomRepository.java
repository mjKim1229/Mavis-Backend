package com.mavis.domain.domains.claim.repository;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimItem;
import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.claim.domain.ClaimType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimCustomRepository {

    Page<Claim> findClaimPages(ClaimType claimType, ClaimStatus claimStatus, Pageable pageable);

    Page<ClaimItem> findClaimItemPages(ClaimType claimType, Pageable pageable);
}
