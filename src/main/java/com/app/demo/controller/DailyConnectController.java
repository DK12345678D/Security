package com.app.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-connect")
public class DailyConnectController {

    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getDailyConnectJobs() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Daily Connect jobs retrieved successfully",
            "jobs", List.of(
                Map.of("id", 1, "title", "Spring Boot Architect", "company", "Security Labs"),
                Map.of("id", 2, "title", "Senior IAM Engineer", "company", "Antigravity Corp")
            )
        ));
    }

    @PostMapping("/admin/connections")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> manageConnectionRequests() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Super Admin managed connection requests successfully"
        ));
    }
}
