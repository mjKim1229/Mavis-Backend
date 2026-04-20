package com.mavis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class MavisApiServerApplication {
    public static void main(String[] args) {
        try {
            Class.forName("com.mavis.domain.domains.review.domain.QReview");
            Class.forName("com.mavis.domain.domains.order.domain.QOrderItem");
            log.info("QCLASS LOADING DONE");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        SpringApplication.run(MavisApiServerApplication.class);
    }
}
