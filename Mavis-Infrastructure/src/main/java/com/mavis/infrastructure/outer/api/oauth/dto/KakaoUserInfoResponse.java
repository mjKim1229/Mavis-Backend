package com.mavis.infrastructure.outer.api.oauth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoUserInfoResponse(long id, KakaoAccount kakaoAccount) {

	public String getEmail() {
		return kakaoAccount.email();
	}

	public String getNickname() {
		return kakaoAccount.profile() != null ? kakaoAccount.profile().nickname() : null;
	}

	@JsonNaming(SnakeCaseStrategy.class)
	private record KakaoAccount(
		String email,
		KakaoProfile profile
	) { }

	@JsonNaming(SnakeCaseStrategy.class)
	private record KakaoProfile(String nickname) { }
}
