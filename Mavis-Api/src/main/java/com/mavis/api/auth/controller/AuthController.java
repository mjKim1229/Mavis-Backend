package com.mavis.api.auth.controller;

import com.mavis.api.auth.dto.UserLoginRequest;
import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.api.auth.dto.UserSignUpRequest;
import com.mavis.api.auth.facade.UserFacade;
import com.mavis.api.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/auths")
public class AuthController {

    private final UserFacade userFacade;
    private final UserService userService;

    @GetMapping("/oauth/kakao")
    public UserOauthResponse register(@RequestParam String code,
                                      @RequestHeader(required = false, name = "Host") String host,
                                      @RequestHeader(required = false, name = "Referer") String referer) {
        if (referer.contains(host)) {
            return userFacade.register(code, "https://garamall.com");
        }else {
            return userFacade.register(code, "http://localhost:3000");
        }
    }

    @GetMapping("/oauth/naver")
    public UserOauthResponse registerNaver(@RequestParam String code) {
        return userFacade.registerNaver(code);
    }

    @PostMapping("/login")
    public UserOauthResponse login(@RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    @DeleteMapping("/withdraw")
    public void withDrawKakaoUser() {

    }

    @PostMapping("/sign-up")
    public void signUp(@RequestBody UserSignUpRequest request) {
        userService.signUp(request);
    }
}
