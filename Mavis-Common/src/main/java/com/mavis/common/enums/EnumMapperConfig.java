package com.mavis.common.enums;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnumMapperConfig {

    @Bean
    public EnumMapper enumMapper() {
        EnumMapper enumMapper = new EnumMapper();
        enumMapper.put(ProductCategory.class.getSimpleName(), ProductCategory.class);
        return enumMapper;
    }
}
