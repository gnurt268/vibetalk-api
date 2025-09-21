package com.gnxrt.vibetalkapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VibetalkApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VibetalkApiApplication.class, args);
    }

}
