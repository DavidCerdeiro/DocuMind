package com.davidcerdeiro.documind.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/health")
public class HealthController {
    
    private final JdbcTemplate jdbcTemplate;
    
    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> healthCheck() {
        try {
            jdbcTemplate.execute("SELECT 1"); 
            
            return ResponseEntity.ok(Map.of("status", "UP", "database", "CONNECTED"));
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of("status", "DOWN", "database", "DISCONNECTED"));
        }
    }
    
}
