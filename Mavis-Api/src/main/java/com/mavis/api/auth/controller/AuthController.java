package com.mavis.api.auth.controller;

import com.mavis.api.auth.dto.UserOauthResponse;
import com.mavis.api.auth.facade.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/auths")
public class AuthController {

    private final UserFacade userFacade;

    @GetMapping("/oauth/kakao")
    public UserOauthResponse register(@RequestParam String code) {
        return userFacade.register(code);
    }

    @GetMapping("/oauth/naver")
    public UserOauthResponse registerNaver(@RequestParam String code) {
        return userFacade.registerNaver(code);
    }

    @DeleteMapping("/withdraw")
    public void withDrawKakaoUser() {

    }
}
