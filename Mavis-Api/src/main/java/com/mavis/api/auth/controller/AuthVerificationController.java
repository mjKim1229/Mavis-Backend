package com.mavis.api.auth.controller;

import com.mavis.api.auth.dto.UserPasswordFoundVerifyCodeRequest;
import com.mavis.api.auth.dto.UserPasswordFoundVerifyCreateRequest;
import com.mavis.api.auth.dto.UserSignUpCodeCreateRequest;
import com.mavis.api.auth.dto.UserSignUpCodeVerifyRequest;
import com.mavis.api.auth.dto.UserEmailChangeCreateRequest;
import com.mavis.api.auth.dto.UserEmailChangeVerifyRequest;
import com.mavis.api.auth.service.AuthVerificationService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "비밀번호 찾기 인증번호 발송")
    @PostMapping("/password")
    public void savePasswordFoundCode(@RequestBody UserPasswordFoundVerifyCreateRequest request) {
        authVerificationService.savePasswordFoundCode(request);
    }

    @Operation(summary = "비밀번호 찾기 인증번호 검증")
    @PostMapping("/password/verify")
    public void verifyPasswordFound(@RequestBody UserPasswordFoundVerifyCodeRequest request) {
        authVerificationService.verifyPasswordFound(request);
    }

    @Operation(summary = "회원가입 인증번호 발송")
    @PostMapping("/sign-up")
    public void saveEmailSignUpCode(@RequestBody UserSignUpCodeCreateRequest request) {
        authVerificationService.saveSignUpCode(request);
    }

    @Operation(summary = "회원가입 인증번호 검증")
    @PostMapping("/sign-up/verify")
    public void verifyEmailSignUpCode(@RequestBody UserSignUpCodeVerifyRequest request) {
        authVerificationService.verifySignUpFound(request);
    }

    @Operation(summary = "이메일 변경 인증번호 발송")
    @PostMapping("/email-change")
    public void saveEmailChangeCode(@RequestBody UserEmailChangeCreateRequest request) {
        authVerificationService.saveEmailChangeCode(request);
    }

    @Operation(summary = "이메일 변경 인증번호 검증")
    @PostMapping("/email-change/verify")
    public void verifyEmailChange(@RequestBody UserEmailChangeVerifyRequest request) {
        authVerificationService.verifyEmailChange(request);
    }
}
