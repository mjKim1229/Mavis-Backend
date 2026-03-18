package com.mavis.admin.domains.delivery.controller;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.delivery.dto.AdminCompleteDeliveryRequest;
import com.mavis.admin.domains.delivery.dto.GetAdminDeliveryResponse;
import com.mavis.admin.domains.delivery.service.AdminDeliveryService;
import com.mavis.admin.domains.order.dto.AdminDeliveryStartRequest;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "관리자 배송 API")
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

    @Operation(summary = "배송 목록 조회 (배송중, 배송완료)")
    @GetMapping
    public PageResponse<GetAdminDeliveryResponse> getAdminDeliveryList(Pageable pageable, DeliveryStatus deliveryStatus) {
        return adminDeliveryService.getAdminDeliveryLists(pageable, deliveryStatus);
    }

    @Operation(summary = "배송 시작 (발주 완료 -> 배송중)")
    @PostMapping("/confirm/{orderId}")
    public void confirmDelivery(@PathVariable Long orderId, @RequestBody AdminDeliveryStartRequest request) {
        adminDeliveryService.confirmOrderDelivery(orderId, request);
    }

    @Operation(summary = "배송 완료 처리 (배송중 -> 배송 완료)")
    @PatchMapping("/complete")
    public void completeDelivery(@RequestBody AdminCompleteDeliveryRequest request) {
        adminDeliveryService.completeDelivery(request);
    }
}
