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
                                .requestMatchers(
                                        "/auth/login",
                                        "/auth/register",
                                        "/auth/password/forgot",
                                        "/auth/password/reset",
                                        "/auth/password/validate-token",
                                        "/auth/password/validate-token/**",
                                        "/api/test/rate-limit"
                                ).permitAll()

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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            String message = "Authentication required";
            String error = "UNAUTHORIZED";

            if (request.getRequestURI().startsWith("/api/")) {
                message = "Please provide a valid authentication token to access this resource";
            } else if (request.getRequestURI().contains("/auth/logout")) {
                message = "You must be logged in to logout";
            } else if (request.getRequestURI().contains("/auth/status")) {
                message = "Authentication token required to check status";
            }

            response.getWriter().write(String.format(
                    "{\"error\":\"%s\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                    error, message, LocalDateTime.now()
            ));
        };
    }
}