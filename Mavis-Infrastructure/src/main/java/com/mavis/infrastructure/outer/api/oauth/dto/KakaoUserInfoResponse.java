package com.mavis.infrastructure.outer.api.oauth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoUserInfoResponse(long id, KakaoAccount kakaoAccount) {

	public String email() { return kakaoAccount != null ? kakaoAccount.email() : null; }
	public String name() { return kakaoAccount != null ? kakaoAccount.name() : null; }
	public String gender() { return kakaoAccount != null ? kakaoAccount.gender() : null; }
	public String birthday() { return kakaoAccount != null ? kakaoAccount.birthday() : null; }
	public String birthyear() { return kakaoAccount != null ? kakaoAccount.birthyear() : null; }
	public String phoneNumber() { return kakaoAccount != null ? kakaoAccount.phoneNumber() : null; }
	public String nickname() {
		return kakaoAccount != null && kakaoAccount.profile() != null ? kakaoAccount.profile().nickname() : null;
	}
	public String profileImageUrl() {
		return kakaoAccount != null && kakaoAccount.profile() != null ? kakaoAccount.profile().profileImageUrl() : null;
	}

	@JsonNaming(SnakeCaseStrategy.class)
	public record KakaoAccount(
		String email,
		String name,
		String gender,
		String birthday,
		String birthyear,
		String phoneNumber,
		KakaoProfile profile
	) { }

	@JsonNaming(SnakeCaseStrategy.class)
	public record KakaoProfile(String nickname, String profileImageUrl) { }
}
