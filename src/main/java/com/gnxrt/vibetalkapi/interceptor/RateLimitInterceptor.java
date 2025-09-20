package com.gnxrt.vibetalkapi.interceptor;

import com.gnxrt.vibetalkapi.config.RateLimitConfig;
import com.gnxrt.vibetalkapi.config.RateLimitConfig.RateLimitType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIpAddress(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();

        RateLimitType limitType = determineRateLimitType(uri, method);

        if (limitType != null) {
            boolean allowed = rateLimitConfig.isAllowed(clientIp, limitType);

            if (!allowed) {
                handleRateLimitExceeded(response, limitType);
                return false;
            }

            addRateLimitHeaders(response, clientIp, limitType);
        }

        return true;
    }

    private RateLimitType determineRateLimitType(String uri, String method) {
        if (uri.contains("/auth/login")) {
            return RateLimitType.LOGIN;
        }
        if (uri.contains("/auth/register")) {
            return RateLimitType.REGISTER;
        }
        if (uri.contains("/auth/password/forgot")) {
            return RateLimitType.FORGOT_PASSWORD;
        }
        if (uri.contains("/auth/password/reset")) {
            return RateLimitType.RESET_PASSWORD;
        }

        if (uri.contains("/messages/send") && "POST".equals(method)) {
            return RateLimitType.SEND_MESSAGE;
        }
        if (uri.contains("/upload") || uri.contains("/avatar")) {
            return RateLimitType.UPLOAD_FILE;
        }

        if (uri.startsWith("/api/")) {
            return RateLimitType.GENERAL_API;
        }

        return null;
    }

    private void addRateLimitHeaders(HttpServletResponse response, String clientIp, RateLimitType limitType) {
        long remainingTokens = rateLimitConfig.getAvailableTokens(clientIp, limitType);

        response.setHeader("X-RateLimit-Limit", String.valueOf(limitType.getCapacity()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remainingTokens));
        response.setHeader("X-RateLimit-Reset", String.valueOf(limitType.getRefillDuration().getSeconds()));
    }

    private void handleRateLimitExceeded(HttpServletResponse response, RateLimitType limitType) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");

        String jsonResponse = String.format(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests for %s. Try again in %d seconds.\",\"retryAfter\":%d}",
                limitType.name().toLowerCase().replace("_", " "),
                limitType.getRefillDuration().getSeconds(),
                limitType.getRefillDuration().getSeconds()
        );

        response.getWriter().write(jsonResponse);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }

        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (xRealIpHeader != null && !xRealIpHeader.isEmpty()) {
            return xRealIpHeader;
        }

        return request.getRemoteAddr();
    }
}