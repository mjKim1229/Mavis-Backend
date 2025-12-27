package com.mavis.admin.domains.delivery.controller;

import com.mavis.admin.domains.delivery.service.AdminDeliveryService;
import com.mavis.admin.domains.order.dto.UpdateAdminOrderConfirmRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/delivery")
@RequiredArgsConstructor
public class AdminDeliveryController {

    private final AdminDeliveryService adminDeliveryService;

    @Operation(summary = "발주 완료 주문 목록 엑셀 다운로드")
    @GetMapping("/excel")
    public ResponseEntity<byte[]> getOrdersByExcel(Pageable pageable) {
        byte[] orderBytes = adminDeliveryService.getOrderByExcel(pageable);
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=배송목록.xlsx"
                )
                .body(orderBytes);
    }

    @Operation(summary = "주문 발주 확인 & 배송 생성")
    @PostMapping("/confirm")
    public void confirmDelivery(@RequestBody UpdateAdminOrderConfirmRequest request) {
        adminDeliveryService.confirmOrderDelivery(request);
    }
}
