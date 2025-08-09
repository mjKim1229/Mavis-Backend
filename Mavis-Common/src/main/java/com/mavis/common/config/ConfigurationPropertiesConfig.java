package com.mavis.common.config;

import com.mavis.common.properties.JwtProperties;
import com.mavis.common.properties.KakaoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
        KakaoProperties.class,
        JwtProperties.class
})
@Configuration
public class ConfigurationPropertiesConfig {
}
