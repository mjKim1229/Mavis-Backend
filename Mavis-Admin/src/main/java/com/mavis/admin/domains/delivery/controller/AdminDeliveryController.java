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

import java.net.URLEncoder;
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

        String fileName = "배송목록.xlsx";

        String encodedFileName = URLEncoder.encode(
                fileName,
                StandardCharsets.UTF_8
        ).replaceAll("\\+", "%20");

        String contentDisposition =
                "attachment; " +
                        "filename=\"delivery.xlsx\"; " +
                        "filename*=UTF-8''" + encodedFileName;

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition
                )
                .body(orderBytes);
    }


    @Operation(summary = "주문 발주 확인 & 배송 생성")
    @PostMapping("/confirm")
    public void confirmDelivery(@RequestBody UpdateAdminOrderConfirmRequest request) {
        adminDeliveryService.confirmOrderDelivery(request);
    }

    @Operation(summary = "주문 상태 변경")
    @PatchMapping("/delivered")
    public void completeDelivery(@RequestBody UpdateAdminDeliveredConfirmRequest request) {
        adminDeliveryService.confirmDelivered(request);
    }
}
