package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, OrderCustomRepository {
    Optional<OrderItem> findByIdAndIsDeletedFalse(Long id);


}
