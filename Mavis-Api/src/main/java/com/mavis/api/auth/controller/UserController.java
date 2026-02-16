package com.mavis.api.auth.controller;

import com.mavis.api.auth.dto.UserAddressRequest;
import com.mavis.api.auth.service.UserService;
import com.mavis.api.order.dto.OrderAddressResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/users")
public class UserController {

    private final UserService userService;

    @PatchMapping("/address")
    @Operation(summary = "기본 배송지 수정")
    public void updateAddress(@RequestBody UserAddressRequest request) {
        userService.updateAddress(request);
    }

    @PostMapping("/address")
    @Operation(summary = "기본 배송지 등록")
    public void createAddress(@RequestBody UserAddressRequest request) {
        userService.createAddress(request);
    }

    @GetMapping("/address")
    @Operation(summary = "기본 배송지 조회")
    public OrderAddressResponse getUserAddress() {
        return userService.getUserAddress();
    }
}
