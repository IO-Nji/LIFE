package io.life.masterdata.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:80"})
public class HealthController {

    private static final String STATUS = "status";

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getHealthStats() {
        Map<String, Object> healthStats = new HashMap<>();
        
        // Basic service information
        healthStats.put("service", "Master Data Service");
        healthStats.put(STATUS, "UP");
        healthStats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        healthStats.put("version", "1.0.0");
        
        // Runtime information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory() / 1024 / 1024 + " MB");
        memory.put("free", runtime.freeMemory() / 1024 / 1024 + " MB");
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        memory.put("max", runtime.maxMemory() / 1024 / 1024 + " MB");
        healthStats.put("memory", memory);
        
        // Database connectivity
        Map<String, Object> database = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            database.put(STATUS, "UP");
            database.put("database", connection.getMetaData().getDatabaseProductName());
            database.put("driver", connection.getMetaData().getDriverName());
        } catch (Exception e) {
            database.put(STATUS, "DOWN");
            database.put("error", e.getMessage());
        }
        healthStats.put("database", database);
        
        // System information
        Map<String, Object> system = new HashMap<>();
        system.put("java_version", System.getProperty("java.version"));
        system.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        system.put("processors", Runtime.getRuntime().availableProcessors());
        healthStats.put("system", system);
        
        // Master data specific information
        Map<String, Object> masterData = new HashMap<>();
        masterData.put("entities", "Workstations, Product Variants, Modules, Parts");
        masterData.put("description", "Provides reference data for manufacturing operations");
        healthStats.put("master_data", masterData);
        
        return ResponseEntity.ok(healthStats);
    }
}