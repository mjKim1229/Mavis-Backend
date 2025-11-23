package com.mavis.domain.domains.order.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;

@Embeddable
@AllArgsConstructor
public class OrderAddress {
    private String address;
    private String addressMemo;
}
