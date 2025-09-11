package com.mavis.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI(ServletContext servletContext) {
        String contextPath = servletContext.getContextPath();
        Server server = new Server().url(contextPath);

        return new OpenAPI()
                .info(new Info()
                        .title("Mavis API 문서")
                        .version("1.0.0")
                        .description("Mavis 서비스 API 명세서"))
                .servers(List.of(server))
                .components(authSetting())
                .addSecurityItem(new SecurityRequirement()
                        .addList("Authorization"));
    }

    private Components authSetting() {
        SecurityScheme authorization = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        return new Components()
                .addSecuritySchemes("Authorization", authorization);
    }
}
