package com.mavis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class MavisApiServerApplication {
    public static void main(String[] args) {
        System.out.println(">>>> [STEP 1] START LOADING QREVIEW");
        try {
            // 이 라인을 넘어가야 다음 로그가 찍힙니다.
            Class.forName("com.mavis.domain.domains.review.domain.QReview");
            System.out.println(">>>> [STEP 2] QREVIEW LOADED");

            Class.forName("com.mavis.domain.domains.order.domain.QOrderItem");
            System.out.println(">>>> [STEP 3] QORDERITEM LOADED");

            // 여기까지 와야 안전한 겁니다.
            System.out.println(">>>> [SUCCESS] ALL QCLASSES READY");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpringApplication.run(MavisApiServerApplication.class);
    }
}
