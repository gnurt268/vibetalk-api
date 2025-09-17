package com.gnxrt.vibetalkapi.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Home", description = "Home endpoint")
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<String> HomeController() {
        return new ResponseEntity<String>("Welcome to my app", HttpStatus.OK);
    }
}
