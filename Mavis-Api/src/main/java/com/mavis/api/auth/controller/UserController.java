package com.mavis.api.auth.controller;

import com.mavis.api.auth.dto.PasswordChangeRequest;
import com.mavis.api.auth.dto.PasswordVerifyRequest;
import com.mavis.api.auth.dto.UserAddressRequest;
import com.mavis.api.auth.dto.UserDetailResponse;
import com.mavis.api.auth.dto.UserProfileResponse;
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

    @GetMapping("/profile")
    @Operation(summary = "마이페이지 조회 - 회원 프로필 (sns, 이름)")
    public UserProfileResponse getUserProfile() {
        return userService.getUserProfile();
    }

    @GetMapping("/profile/details")
    @Operation(summary = "회원 상세 정보 조회 - 회원 프로필 클릭시")
    public UserDetailResponse getUserDetail() {
        return userService.getUserDetail();
    }

    @PostMapping("/verify-password")
    @Operation(summary = "비밀번호 확인")
    public boolean verifyPassword(@RequestBody PasswordVerifyRequest request) {
        return userService.verifyPassword(request.password());
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경")
    public void changePassword(@RequestBody PasswordChangeRequest request) {
        userService.changePassword(request.currentPassword(), request.newPassword());
    }
}
