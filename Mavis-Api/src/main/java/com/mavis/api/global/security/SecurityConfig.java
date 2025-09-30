package com.mavis.api.global.security;

import com.mavis.common.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenUtil jwtTokenUtil;
    private final FilterConfig filterConfig;
    private final CustomAuthenticationEntryPoint entryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .with(filterConfig, Customizer.withDefaults())
                .exceptionHandling((exceptionConfig) ->
                        exceptionConfig.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests((requests) ->
                        requests.requestMatchers("/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**",
                                        "/v3/api-docs")
                                .permitAll()
                                .requestMatchers("/api/v1/**")
                                .permitAll()
                                .anyRequest()
                                .hasRole("USER")
                );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**",
                        "/v3/api-docs")
                .requestMatchers("/v1/api/products/**")
                .requestMatchers("/v1/api/auths/**")
                .requestMatchers(HttpMethod.GET, "/v1/api/inquiry/product/**")
                .requestMatchers(HttpMethod.GET, "/v1/api/review/**");
    }
}
