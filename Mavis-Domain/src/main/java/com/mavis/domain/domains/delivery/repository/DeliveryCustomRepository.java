package com.mavis.domain.domains.delivery.repository;

import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.dto.AdminDeliveryRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryCustomRepository {
    Page<AdminDeliveryRow> findDeliveryRows(Pageable pageable, DeliveryStatus deliveryStatus);
    List<Delivery> findDeliveriesByStatusAndDateRange(DeliveryStatus deliveryStatus, LocalDate startDate, LocalDate endDate);
}
