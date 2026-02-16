package com.mavis.api.auth.controller;

import com.mavis.api.auth.dto.UserLoginRequest;
import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.api.auth.dto.UserSignUpRequest;
import com.mavis.api.auth.dto.UsernameCheckResponse;
import com.mavis.api.auth.facade.UserFacade;
import com.mavis.api.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/auths")
public class AuthController {

    private final UserFacade userFacade;
    private final UserService userService;

    // ...existing code...

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    public UserOauthResponse tokenRefresh(@RequestHeader(value = "refreshToken") String refreshToken) {
        return userService.tokenRefresh(refreshToken);
    }

    @GetMapping("/check-username")
    @Operation(summary = "username 중복 확인")
    public UsernameCheckResponse checkUsername(@RequestParam String username) {
        boolean available = userService.isUsernameAvailable(username);
        return new UsernameCheckResponse(username, available);
    }
}
