package com.women.safety.features.authentication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Hello from Public endpoint 🚀 (No token required)";
    }

    @GetMapping("/protected/hello")
    public String protectedHello() {
        return "Hello from Protected endpoint 🔒 (Valid JWT required)";
    }

    @GetMapping("/adminOnly/hello")
    public String adminOnlyHello() {
        return "Hello to admin only 🔒 (Valid JWT required)";
    }
}
