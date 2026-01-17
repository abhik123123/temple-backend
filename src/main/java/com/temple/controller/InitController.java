package com.temple.controller;

import com.temple.component.DataInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * InitController - Endpoints to manually load/clear data
 *
 * Available Endpoints:
 * POST   /api/init-data    - Load sample data from data.sql
 * POST   /api/clear-data   - Clear all data from database
 * GET    /api/db-status    - Get database status
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class InitController {

    @Autowired
    private DataInitializer dataInitializer;

    /**
     * Load sample data from data.sql file
     * POST /api/init-data
     */
    @PostMapping("/init-data")
    public ResponseEntity<Map<String, Object>> initializeData() {
        Map<String, Object> response = new HashMap<>();

        String result = dataInitializer.loadDataFromSQL();

        boolean success = result.contains("✅");
        response.put("success", success);
        response.put("message", result);

        if (success) {
            response.put("totalRecords", 47);
            response.put("source", "data.sql");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Clear all data from database
     * POST /api/clear-data
     */
    @PostMapping("/clear-data")
    public ResponseEntity<Map<String, Object>> clearData() {
        Map<String, Object> response = new HashMap<>();

        String result = dataInitializer.clearAllData();

        boolean success = result.contains("✅");
        response.put("success", success);
        response.put("message", result);

        return ResponseEntity.ok(response);
    }

    /**
     * Get database status
     * GET /api/db-status
     */
    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();

        String status = dataInitializer.getDatabaseStatus();

        response.put("success", true);
        response.put("status", status);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Temple Backend API is running");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}

