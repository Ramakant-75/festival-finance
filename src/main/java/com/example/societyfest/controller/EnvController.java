package com.example.societyfest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/env")
public class EnvController {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping
    public Map<String, String> getActiveProfile() {
        return Map.of("env", activeProfile);
    }
}
