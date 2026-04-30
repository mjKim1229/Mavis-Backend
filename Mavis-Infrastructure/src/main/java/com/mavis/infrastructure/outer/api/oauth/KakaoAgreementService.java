package com.mavis.infrastructure.outer.api.oauth;

import com.mavis.infrastructure.outer.api.oauth.client.kakao.KakaoInfoClient;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoServiceTermsResponse.KakaoServiceTerm;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAgreementService {

    private final KakaoInfoClient kakaoInfoClient;

    public List<KakaoServiceTerm> getAgreements(String bearerAccessToken) {
        try {
            List<KakaoServiceTerm> terms = kakaoInfoClient.getServiceTerms(bearerAccessToken).serviceTerms();
            return terms != null ? terms : List.of();
        } catch (FeignException.NotFound e) {
            log.info("[Kakao 동의 조회] 동의 항목 없음 - 마케팅 동의 false 처리", e);
            return List.of();
        }
    }
}
