package com.mavis.api.auth.controller;

import com.mavis.api.auth.facade.UserFacade;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auths")
public class AuthController {

    private final UserFacade userFacade;

    @GetMapping("/oauth/kakao")
    public KakaoUserInfoResponse register(@RequestParam String code) {
        return userFacade.register(code);
    }
}
