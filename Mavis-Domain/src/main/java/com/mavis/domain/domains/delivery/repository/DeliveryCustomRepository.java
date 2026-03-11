package com.mavis.domain.domains.delivery.repository;

import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeliveryCustomRepository {
    Page<Delivery> findDeliveryPagesByDeliveryStatus(Pageable pageable, DeliveryStatus deliveryStatus);
}
