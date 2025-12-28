package com.mavis.common.config;

import com.mavis.common.properties.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
        KakaoProperties.class,
        JwtProperties.class,
        NaverProperties.class,
        MailProperties.class,
        TossPaymentsProperties.class
})
@Configuration
public class ConfigurationPropertiesConfig {
}
