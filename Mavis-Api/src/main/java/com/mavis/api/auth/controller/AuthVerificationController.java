package com.mavis.api.auth.controller;

import com.mavis.api.auth.dto.UserPasswordFoundVerifyCodeRequest;
import com.mavis.api.auth.dto.UserPasswordFoundVerifyCreateRequest;
import com.mavis.api.auth.service.AuthVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/api/auths/verify")
@RestController
@RequiredArgsConstructor
public class AuthVerificationController {

    private final AuthVerificationService authVerificationService;

    @PostMapping("/password")
    public void savePasswordFoundCode(@RequestBody UserPasswordFoundVerifyCreateRequest request) {
        authVerificationService.savePasswordFoundCode(request);
    }

    @PostMapping("/password/verify")
    public void verifyPasswordFound(@RequestBody UserPasswordFoundVerifyCodeRequest request) {
        authVerificationService.verifyPasswordFound(request);
    }
}
