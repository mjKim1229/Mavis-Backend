package com.mavis.infrastructure.outer.api.oauth;

import com.mavis.infrastructure.outer.api.oauth.client.naver.NaverInfoClient;
import com.mavis.infrastructure.outer.api.oauth.dto.NaverAgreementInfo;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverAgreementService {

    private final NaverInfoClient naverInfoClient;

    public List<NaverAgreementInfo> getAgreements(String bearerAccessToken) {
        try {
            return naverInfoClient.getAgreements(bearerAccessToken).agreementInfos();
        } catch (FeignException.NotFound e) {
            log.info("[Naver 동의 조회] 동의 항목 없음 - 마케팅 동의 false 처리", e);
            return List.of();
        }
    }
}
