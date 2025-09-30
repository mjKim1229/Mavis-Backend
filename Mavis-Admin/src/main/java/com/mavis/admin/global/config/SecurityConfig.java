package com.mavis.admin.global.config;

import com.mavis.admin.global.security.CustomAuthenticationEntryPoint;
import com.mavis.admin.global.security.FilterConfig;
import com.mavis.admin.global.security.JwtTokenFilter;
import com.mavis.common.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                )
                .addFilterBefore(new JwtTokenFilter(jwtTokenUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**",
                        "/v3/api-docs")
                .requestMatchers("/v1/api/admin/auth");
    }
}
