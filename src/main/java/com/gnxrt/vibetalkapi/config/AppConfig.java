package com.gnxrt.vibetalkapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class AppConfig {

    private final JwtConstant jwtConstant;

    public AppConfig(JwtConstant jwtConstant) {
        this.jwtConstant = jwtConstant;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize ->
                        authorize
                                // Public endpoints - no authentication required
                                .requestMatchers(
                                        "/auth/login",
                                        "/auth/register",
                                        "/auth/password/forgot",
                                        "/auth/password/reset",
                                        "/auth/password/validate-token",
                                        "/auth/password/validate-token/**"
                                ).not().authenticated()

                                // Authenticated endpoints
                                .requestMatchers(
                                        "/auth/logout",
                                        "/auth/refresh-token",
                                        "/auth/status"
                                ).authenticated()

                                .requestMatchers("/api/**").authenticated()
                                .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtTokenValidator(jwtConstant), BasicAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(customAuthenticationEntryPoint())
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration cfg = new CorsConfiguration();

                cfg.setAllowedOrigins(Arrays.asList(
                        "http://localhost:3030",
                        "http://localhost:3000"
                ));
                cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                cfg.setAllowCredentials(true);
                cfg.setAllowedHeaders(Arrays.asList("*"));
                cfg.setExposedHeaders(Arrays.asList("Authorization"));
                cfg.setMaxAge(3600L);

                return cfg;
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            String message = "You are already logged in";
            if (request.getRequestURI().contains("/login")) {
                message = "Already logged in. Please logout first to login with different account.";
            } else if (request.getRequestURI().contains("/register")) {
                message = "Already logged in. Please logout first to register new account.";
            }

            response.getWriter().write(String.format(
                    "{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                    "ACCESS_DENIED", message, LocalDateTime.now()
            ));
        };
    }
}