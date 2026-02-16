package com.mavis.api.auth.controller;

import com.mavis.api.auth.dto.UserAddressUpdateRequest;
import com.mavis.api.auth.service.UserService;
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
    public void updateAddress(@RequestBody UserAddressUpdateRequest request) {
        userService.updateAddress(request);
    }
}
