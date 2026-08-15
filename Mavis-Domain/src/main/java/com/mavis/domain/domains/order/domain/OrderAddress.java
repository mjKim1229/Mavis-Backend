package com.mavis.domain.domains.order.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderAddress {
    private String receiverName;
    private String receiverPhone;
    private String zipCode;
    private String address;
    private String addressDetail;
    private String addressMemo;

    public String getFullAddress() {
        if (addressDetail == null) {
            return address;
        }
        return address + " " + addressDetail;
    }
}
