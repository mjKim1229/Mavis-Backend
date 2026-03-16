package com.mavis.api.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final FilterConfig filterConfig;
    private final CustomAuthenticationEntryPoint entryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .with(filterConfig, Customizer.withDefaults())
                .exceptionHandling((exceptionConfig) ->
                        exceptionConfig.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests((requests) ->
                        requests
                                .requestMatchers("/v1/api/review/user/**").hasRole("USER")
                                .requestMatchers("/v1/api/inquiry/user/**").hasRole("USER")
                                .requestMatchers("/v1/api/order/**").hasRole("USER")
                                .requestMatchers("/v1/api/cart/**").hasRole("USER")
                                .requestMatchers("/v1/api/favorites/**").hasRole("USER")
                                .requestMatchers("/v1/api/users/**").hasRole("USER")
                                .requestMatchers(
                                        "/v1/api/auths/withdraw",
                                        "/v1/api/auths/oauth/kakao/withdraw",
                                        "/v1/api/auths/oauth/naver/withdraw",
                                        "/v1/api/auths/verify/email-change/**"
                                ).hasRole("USER")
                                .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**",
                        "/v3/api-docs")
                .requestMatchers("/v1/api/auths/login")
                .requestMatchers("/v1/api/auths/sign-up")
                .requestMatchers("/v1/api/auths/oauth/kakao")
                .requestMatchers("/v1/api/auths/oauth/naver")
                .requestMatchers("/v1/api/products/**")
                .requestMatchers("/v1/api/inquiry/product/**")
                .requestMatchers(HttpMethod.GET, "/v1/api/review/product/**")
                .requestMatchers(HttpMethod.POST, "/v1/api/order/deposit-callback");
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "https://www.garamall.com"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
