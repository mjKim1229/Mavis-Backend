package com.mavis.infrastructure.outer.api.oauth.config;

import com.mavis.infrastructure.outer.api.oauth.properties.KakaoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
        KakaoProperties.class,
})
@Configuration
public class ConfigurationPropertiesConfig {
}
