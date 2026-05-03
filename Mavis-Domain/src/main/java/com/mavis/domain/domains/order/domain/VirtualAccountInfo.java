package com.mavis.domain.domains.order.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VirtualAccountInfo {

    private String virtualAccountNumber;

    private String virtualAccountBankCode;

    private LocalDateTime virtualAccountDueDate;

    private String virtualAccountDepositorName;
}
