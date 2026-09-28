package com.mavis.domain.domains.claim.repository;

import com.mavis.domain.domains.claim.domain.ClaimItem;
import com.mavis.domain.domains.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimItemRepository extends JpaRepository<ClaimItem, Long> {

    boolean existsByOrderItem(OrderItem orderItem);
}
