package com.mavis.infrastructure.outer.api.oauth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoUserInfoResponse(long id, KakaoAccount kakaoAccount) {

	@JsonNaming(SnakeCaseStrategy.class)
	public record KakaoAccount(
		String email,
		String name,
		String gender,
		String birthDay,
		String birthYear,
		String phoneNumber,
		KakaoProfile profile
	) { }

	@JsonNaming(SnakeCaseStrategy.class)
	public record KakaoProfile(String nickname, String image) { }
}
