package io.life;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Order Processing Service.
 * This microservice handles the management and processing of all order types:
 * - Manufacturing Orders
 * - Assembly Orders
 * - Production Control Orders
 * - Supplier Orders
 */
@SpringBootApplication
@EnableScheduling
public class OrderProcessingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderProcessingServiceApplication.class, args);
    }
}
