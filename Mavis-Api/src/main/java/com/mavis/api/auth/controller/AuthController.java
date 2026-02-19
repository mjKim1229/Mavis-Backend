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

    @Operation(summary = "카카오 로그인 code 전송 후 로그인 처리", description = "code만 보내면 됩니다. (Host, Origin)은 안 보내도 됨")
    @GetMapping("/oauth/kakao")
    public UserOauthResponse register(@RequestParam String code,
                                      @RequestHeader(required = false, name = "Host") String host,
                                      @RequestHeader(required = false, name = "Referer") String referer) {
        return userFacade.register(code, "https://www.garamall.com");
    }

    @Operation(summary = "네이버 로그인")
    @GetMapping("/oauth/naver")
    public UserOauthResponse registerNaver(@RequestParam String code) {
        return userFacade.registerNaver(code);
    }

    @Operation(summary = "회원탈퇴")
    @DeleteMapping("/withdraw")
    public void withDrawNaverUser() {
        userFacade.withDraw();
    }

    @Operation(summary = "일반 회원가입")
    @PostMapping("/sign-up")
    public void signUp(@RequestBody UserSignUpRequest request) {
        userService.signUp(request);
    }

    @Operation(summary = "일반 로그인")
    @PostMapping("/login")
    public UserOauthResponse login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

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
