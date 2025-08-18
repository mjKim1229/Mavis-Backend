package com.mavis.api.auth.controller;

import com.mavis.api.auth.dto.UserKakaoOauthResponse;
import com.mavis.api.auth.facade.UserFacade;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auths")
public class AuthController {

    private final UserFacade userFacade;

    @GetMapping("/oauth/kakao")
    public UserKakaoOauthResponse register(@RequestParam String code) {
        return userFacade.register(code);
    }

    @GetMapping("/oauth/naver")
    public void registerNaver(@RequestParam String code) {
        userFacade.registerNaver(code);
    }

    @DeleteMapping("/withdraw")
    public void withDrawKakaoUser() {

    }
}
