package com.mavis.domain.domains.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PendingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;

    private int amount;

    @Builder.Default
    private boolean isConfirmed = false;

    public void confirmed() {
        this.isConfirmed = true;
    }
}
