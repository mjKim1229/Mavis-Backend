package com.mavis.admin.domains.order.dto;

import java.util.List;

public record UpdateAdminOrderConfirmRequest(
        List<Long> orderIds
        //TODO 송장번호등 배송 생성시 추가 정보
) {
}