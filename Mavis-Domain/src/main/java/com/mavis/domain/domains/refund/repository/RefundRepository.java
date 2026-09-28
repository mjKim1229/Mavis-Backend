package com.mavis.domain.domains.refund.repository;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimType;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.refund.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    @Query("SELECT r FROM Refund r JOIN FETCH r.payment WHERE r.claim IN :claims")
    List<Refund> findByClaimInWithPayment(@Param("claims") List<Claim> claims);

    @Query("SELECT r FROM Refund r JOIN FETCH r.claim c WHERE c.order IN :orders AND c.claimType = :claimType")
    List<Refund> findByOrderInAndClaimType(@Param("orders") List<Order> orders, @Param("claimType") ClaimType claimType);
}
