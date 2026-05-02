package com.mavis.infrastructure.outer.api.oauth;

import com.mavis.infrastructure.outer.api.oauth.client.kakao.KakaoInfoClient;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoServiceTermsResponse;
import com.mavis.infrastructure.outer.api.oauth.dto.KakaoServiceTermsResponse.KakaoServiceTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAgreementService {

    private final KakaoInfoClient kakaoInfoClient;

    public MarketingConsent getMarketingConsent(String bearerAccessToken) {
        List<KakaoServiceTerm> terms = getAgreements(bearerAccessToken);
        return new MarketingConsent(
                isAgreed(terms, KakaoTermTag.MARKETING_EMAIL),
                isAgreed(terms, KakaoTermTag.MARKETING_SMS)
        );
    }

    private boolean isAgreed(List<KakaoServiceTerm> terms, KakaoTermTag termTag) {
        return terms.stream().anyMatch(t -> termTag.getTag().equals(t.tag()) && t.agreed());
    }

    private List<KakaoServiceTerm> getAgreements(String bearerAccessToken) {
        KakaoServiceTermsResponse serviceTermsResponse = kakaoInfoClient.getServiceTerms(bearerAccessToken);
        log.info("[Kakao 동의 조회] 동의 항목 조회 - {} 항목", serviceTermsResponse);
        List<KakaoServiceTerm> kakaoServiceTerms = serviceTermsResponse.serviceTerms();
        if (kakaoServiceTerms == null) {
            log.info("[Kakao 동의 조회] 동의 항목 없음 - 마케팅 동의 false 처리");
            return List.of();
        }
        return kakaoServiceTerms;
    }
}
