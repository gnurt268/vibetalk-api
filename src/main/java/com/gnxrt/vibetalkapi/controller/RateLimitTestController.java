package com.gnxrt.vibetalkapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class RateLimitTestController {

    @GetMapping("/rate-limit")
    public String testRateLimit() {
        return "Rate limit test successful! Check headers for rate limit info.";
    }
}