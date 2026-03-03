package com.mavis.admin.domains.delivery.controller;

import com.mavis.admin.domains.delivery.dto.UpdateAdminDeliveredConfirmRequest;
import com.mavis.admin.domains.delivery.service.AdminDeliveryService;
import com.mavis.admin.domains.order.dto.UpdateAdminOrderConfirmRequest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/v1/api/delivery")
@RequiredArgsConstructor
public class AdminDeliveryController {

    private final AdminDeliveryService adminDeliveryService;

    @Operation(summary = "발주 완료 주문 목록 엑셀 다운로드")
    @GetMapping("/excel")
    public ResponseEntity<byte[]> getOrdersByExcel(Pageable pageable) {
        byte[] orderBytes = adminDeliveryService.getOrderByExcel(pageable);

        String encodedFileName = UriUtils.encode("배송목록.xlsx", StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(orderBytes);
    }

    @Operation(summary = "발주 완료 페이지에서 배송 생성")
    @PostMapping("/confirm/{orderId}")
    public void confirmDelivery(@PathVariable Long orderId, @RequestBody UpdateAdminOrderConfirmRequest request) {
        adminDeliveryService.confirmOrderDelivery(orderId, request);
    }

    @Operation(summary = "주문 상태 변경")
    @PatchMapping("/delivered")
    public void completeDelivery(@RequestBody UpdateAdminDeliveredConfirmRequest request) {
        adminDeliveryService.confirmDelivered(request);
    }
}
