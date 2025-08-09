package com.mavis.infrastructure.outer.api.oauth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoUserInfoResponse(long id, KakaoAccount kakaoAccount) {

	private String getEmail() {
		return kakaoAccount.email();
	}

	private record KakaoAccount(String email) { }
}
