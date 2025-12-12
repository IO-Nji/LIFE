package io.life.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:80"})
public class HealthController {

    private static final String SERVICE_KEY = "service";
    private static final String TARGET_KEY = "target";
    private static final String LOCALHOST_URL = "http://localhost:";

    @Value("${USER_SERVICE_PORT:8012}")
    private String userServicePort;

    @Value("${MASTERDATA_SERVICE_PORT:8013}")
    private String masterdataServicePort;

    @Value("${INVENTORY_SERVICE_PORT:8014}")
    private String inventoryServicePort;

    @Value("${ORDER_PROCESSING_SERVICE_PORT:8015}")
    private String orderProcessingServicePort;

    @Value("${SIMAL_INTEGRATION_SERVICE_PORT:8085}")
    private String simalIntegrationServicePort;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getHealthStats() {
        Map<String, Object> healthStats = new HashMap<>();
        
        // Basic service information
        healthStats.put(SERVICE_KEY, "API Gateway");
        healthStats.put("status", "UP");
        healthStats.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        healthStats.put("version", "1.0.0");
        healthStats.put("port", "8011");
        
        // Runtime information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory() / 1024 / 1024 + " MB");
        memory.put("free", runtime.freeMemory() / 1024 / 1024 + " MB");
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
        memory.put("max", runtime.maxMemory() / 1024 / 1024 + " MB");
        healthStats.put("memory", memory);
        
        // System information
        Map<String, Object> system = new HashMap<>();
        system.put("java_version", System.getProperty("java.version"));
        system.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        system.put("processors", Runtime.getRuntime().availableProcessors());
        healthStats.put("system", system);
        
        // API Gateway specific information
        Map<String, Object> gateway = new HashMap<>();
        gateway.put("functions", "Request Routing, Load Balancing, Authentication, CORS Handling");
        gateway.put("description", "Central entry point for all microservices");
        gateway.put("framework", "Spring Cloud Gateway");
        
        // Configured routes
        List<Map<String, String>> routes = new ArrayList<>();
        
        Map<String, String> userRoute = new HashMap<>();
        userRoute.put("path", "/api/users/**");
        userRoute.put(SERVICE_KEY, "user-service");
        userRoute.put(TARGET_KEY, LOCALHOST_URL + userServicePort);
        routes.add(userRoute);
        
        Map<String, String> masterdataRoute = new HashMap<>();
        masterdataRoute.put("path", "/api/masterdata/**");
        masterdataRoute.put(SERVICE_KEY, "masterdata-service");
        masterdataRoute.put(TARGET_KEY, LOCALHOST_URL + masterdataServicePort);
        routes.add(masterdataRoute);
        
        Map<String, String> inventoryRoute = new HashMap<>();
        inventoryRoute.put("path", "/api/inventory/**");
        inventoryRoute.put(SERVICE_KEY, "inventory-service");
        inventoryRoute.put(TARGET_KEY, LOCALHOST_URL + inventoryServicePort);
        routes.add(inventoryRoute);
        
        Map<String, String> orderRoute = new HashMap<>();
        orderRoute.put("path", "/api/orders/**, /api/assembly/**");
        orderRoute.put(SERVICE_KEY, "order-processing-service");
        orderRoute.put(TARGET_KEY, LOCALHOST_URL + orderProcessingServicePort);
        routes.add(orderRoute);
        
        gateway.put("routes", routes);
        healthStats.put("api_gateway", gateway);
        
        // CORS configuration
        Map<String, Object> cors = new HashMap<>();
        cors.put("allowed_origins", "http://localhost:3000, http://localhost:5173, http://localhost:80");
        cors.put("allowed_methods", "GET, POST, PUT, DELETE, OPTIONS");
        cors.put("allowed_headers", "*");
        cors.put("allow_credentials", true);
        healthStats.put("cors", cors);
        
        return ResponseEntity.ok(healthStats);
    }
}