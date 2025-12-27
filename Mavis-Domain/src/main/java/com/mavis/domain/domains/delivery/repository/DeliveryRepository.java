package com.mavis.domain.domains.delivery.repository;

import com.mavis.domain.domains.delivery.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByIdInAndIsDeletedFalse(List<Long> ids);
}
