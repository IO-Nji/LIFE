package io.life.order.service;

import io.life.order.dto.ProductionOrderDTO;
import io.life.order.repository.ProductionOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing production planning and SimAL integration.
 * Coordinates with SimAL Integration Service to schedule production orders.
 * Tracks production progress and updates order status.
 */
@Service
@Transactional
public class ProductionPlanningService {

    private static final Logger logger = LoggerFactory.getLogger(ProductionPlanningService.class);
    private static final Long PRODUCTION_CONTROL_WORKSTATION_ID = 20L; // Production Control workstation
    private static final Long ASSEMBLY_CONTROL_WORKSTATION_ID = 21L;   // Assembly Control workstation
    private static final String PRODUCTION_ORDER_NOT_FOUND_ERROR = "Production order not found: ";
    private static final String SIMAL_SCHEDULED_ORDERS_PATH = "/simal/scheduled-orders/";

    private final ProductionOrderService productionOrderService;
    private final ProductionControlOrderService productionControlOrderService;
    private final AssemblyControlOrderService assemblyControlOrderService;
    private final RestTemplate restTemplate;

    @Value("${simal.api.base-url:http://localhost:8016/api}")
    private String simalApiBaseUrl;

    public ProductionPlanningService(
            ProductionOrderRepository productionOrderRepository,
            ProductionOrderService productionOrderService,
            ProductionControlOrderService productionControlOrderService,
            AssemblyControlOrderService assemblyControlOrderService,
            RestTemplate restTemplate) {
        this.productionOrderService = productionOrderService;
        this.productionControlOrderService = productionControlOrderService;
        this.assemblyControlOrderService = assemblyControlOrderService;
        this.restTemplate = restTemplate;
    }

    /**
     * Submit a production order to SimAL for scheduling.
     * Sends order details and receives a schedule ID and estimated duration.
     */
    public ProductionOrderDTO submitProductionOrderToSimal(Long productionOrderId) {
        ProductionOrderDTO order = productionOrderService.getProductionOrderById(productionOrderId)
                .orElseThrow(() -> new RuntimeException(PRODUCTION_ORDER_NOT_FOUND_ERROR + productionOrderId));

        // Check if already submitted
        if ("SUBMITTED".equals(order.getStatus()) || "SCHEDULED".equals(order.getStatus())) {
            logger.warn("Production order {} already submitted or scheduled", order.getProductionOrderNumber());
            return order;
        }

        try {
            // Create request payload for SimAL
            SimalProductionOrderRequest request = new SimalProductionOrderRequest();
            request.setProductionOrderNumber(order.getProductionOrderNumber());
            request.setSourceCustomerOrderId(order.getSourceCustomerOrderId());
            request.setDueDate(order.getDueDate());
            request.setPriority(order.getPriority());
            request.setNotes(order.getNotes());

            // Send to SimAL API
            String url = simalApiBaseUrl + "/simal/production-order";
            HttpEntity<SimalProductionOrderRequest> requestEntity = new HttpEntity<>(request);
            @SuppressWarnings("null")
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                @SuppressWarnings("null")
                String scheduleId = (String) responseBody.get("scheduleId");
                Integer estimatedDuration = ((Number) responseBody.get("estimatedDuration")).intValue();
                String estimatedCompletionStr = (String) responseBody.get("estimatedCompletion");

                // Update production order with schedule information
                ProductionOrderDTO updatedOrder = productionOrderService.linkToSimalSchedule(
                        productionOrderId,
                        scheduleId,
                        estimatedDuration,
                        LocalDateTime.parse(estimatedCompletionStr)
                );

                // Update status to SUBMITTED
                updatedOrder = productionOrderService.updateProductionOrderStatus(productionOrderId, "SUBMITTED");
                logger.info("Submitted production order {} to SimAL with schedule {}", 
                        order.getProductionOrderNumber(), scheduleId);

                return updatedOrder;
            } else {
                logger.error("Failed to submit production order {} to SimAL: HTTP {}", 
                        order.getProductionOrderNumber(), response.getStatusCode());
                throw new RuntimeException("SimAL API returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error submitting production order {} to SimAL: {}", 
                    order.getProductionOrderNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to submit to SimAL: " + e.getMessage());
        }
    }

    /**
     * Get scheduled tasks for a production order from SimAL.
     */
    public List<Map<String, Object>> getScheduledTasks(String simalScheduleId) {
        try {
            String url = simalApiBaseUrl + SIMAL_SCHEDULED_ORDERS_PATH + simalScheduleId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                @SuppressWarnings("null")
                List<Map<String, Object>> tasks = (List<Map<String, Object>>) responseBody.get("tasks");
                logger.info("Retrieved {} scheduled tasks for schedule {}", tasks.size(), simalScheduleId);
                return tasks;
            } else {
                logger.warn("Failed to get scheduled tasks for schedule {}: HTTP {}", 
                        simalScheduleId, response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Error retrieving scheduled tasks for schedule {}: {}", simalScheduleId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Update production order progress from SimAL.
     * Called periodically or when receiving status update notifications from SimAL.
     */
    public ProductionOrderDTO updateProductionProgress(Long productionOrderId) {
        ProductionOrderDTO order = productionOrderService.getProductionOrderById(productionOrderId)
                .orElseThrow(() -> new RuntimeException(PRODUCTION_ORDER_NOT_FOUND_ERROR + productionOrderId));

        if (order.getSimalScheduleId() == null) {
            logger.warn("Production order {} not linked to SimAL schedule", order.getProductionOrderNumber());
            return order;
        }

        try {
            String url = simalApiBaseUrl + SIMAL_SCHEDULED_ORDERS_PATH + order.getSimalScheduleId() + "/status";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                @SuppressWarnings("null")
                String status = (String) responseBody.get("status");

                // Update production order status based on SimAL status
                String newStatus = mapSimalStatusToPOStatus(status);
                if (!newStatus.equals(order.getStatus())) {
                    order = productionOrderService.updateProductionOrderStatus(productionOrderId, newStatus);
                    logger.info("Updated production order {} to status {} based on SimAL", 
                            order.getProductionOrderNumber(), newStatus);
                }

                return order;
            } else {
                logger.warn("Failed to get production progress for schedule {}: HTTP {}", 
                        order.getSimalScheduleId(), response.getStatusCode());
                return order;
            }
        } catch (Exception e) {
            logger.error("Error updating production progress for order {}: {}", 
                    order.getProductionOrderNumber(), e.getMessage());
            return order;
        }
    }

    /**
     * Start production for a scheduled order in SimAL.
     */
    public ProductionOrderDTO startProduction(Long productionOrderId) {
        ProductionOrderDTO order = productionOrderService.getProductionOrderById(productionOrderId)
                .orElseThrow(() -> new RuntimeException(PRODUCTION_ORDER_NOT_FOUND_ERROR + productionOrderId));

        if (!"SCHEDULED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot start production - order status is " + order.getStatus());
        }

        try {
            String url = simalApiBaseUrl + SIMAL_SCHEDULED_ORDERS_PATH + order.getSimalScheduleId() + "/start";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HashMap<>(), Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                order = productionOrderService.updateProductionOrderStatus(productionOrderId, "IN_PRODUCTION");
                logger.info("Started production for order {} in SimAL", order.getProductionOrderNumber());
                return order;
            } else {
                throw new RuntimeException("SimAL API returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error starting production for order {}: {}", order.getProductionOrderNumber(), e.getMessage());
            throw new RuntimeException("Failed to start production: " + e.getMessage());
        }
    }

    /**
     * Mark production as complete for a scheduled order in SimAL.
     */
    public ProductionOrderDTO completeProduction(Long productionOrderId) {
        ProductionOrderDTO order = productionOrderService.getProductionOrderById(productionOrderId)
                .orElseThrow(() -> new RuntimeException(PRODUCTION_ORDER_NOT_FOUND_ERROR + productionOrderId));

        try {
            String url = simalApiBaseUrl + SIMAL_SCHEDULED_ORDERS_PATH + order.getSimalScheduleId() + "/complete";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HashMap<>(), Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                order = productionOrderService.completeProductionOrder(productionOrderId);
                logger.info("Completed production for order {} in SimAL", order.getProductionOrderNumber());
                return order;
            } else {
                throw new RuntimeException("SimAL API returned error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error completing production for order {}: {}", order.getProductionOrderNumber(), e.getMessage());
            throw new RuntimeException("Failed to complete production: " + e.getMessage());
        }
    }

    /**
     * Process SimAL output to create ProductionControlOrder and AssemblyControlOrder.
     * Called when production order enters IN_PRODUCTION status.
     */
    public void createControlOrdersFromSimalSchedule(Long productionOrderId, String simalScheduleId) {
        ProductionOrderDTO order = productionOrderService.getProductionOrderById(productionOrderId)
                .orElseThrow(() -> new RuntimeException(PRODUCTION_ORDER_NOT_FOUND_ERROR + productionOrderId));

        try {
            // Get scheduled tasks from SimAL
            List<Map<String, Object>> tasks = getScheduledTasks(simalScheduleId);
            
            if (tasks != null && !tasks.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startTime = order.getExpectedCompletionTime() != null ? 
                    order.getExpectedCompletionTime() : now.plusHours(1);

                // Create ProductionControlOrder
                productionControlOrderService.createControlOrder(
                        productionOrderId,
                        PRODUCTION_CONTROL_WORKSTATION_ID,
                        simalScheduleId,
                        order.getPriority(),
                        now,
                        startTime,
                        "Production process for order " + order.getProductionOrderNumber(),
                        "Check output quality, verify dimensions, inspect surface finish",
                        "Follow safety protocols, use protective equipment",
                        order.getEstimatedDuration() != null ? order.getEstimatedDuration() : 120
                );

                // Create AssemblyControlOrder to be started after production
                assemblyControlOrderService.createControlOrder(
                        productionOrderId,
                        ASSEMBLY_CONTROL_WORKSTATION_ID,
                        simalScheduleId,
                        order.getPriority(),
                        startTime.plusMinutes(order.getEstimatedDuration() != null ? order.getEstimatedDuration() : 120),
                        startTime.plusMinutes(order.getEstimatedDuration() != null ? order.getEstimatedDuration() * 2 : 240),
                        "Assembly instructions for order " + order.getProductionOrderNumber(),
                        "Verify all components assembled, test functionality",
                        "Test all features work correctly",
                        "Package according to customer requirements",
                        120
                );

                logger.info("Created production and assembly control orders for production order {} from SimAL schedule {}", 
                        order.getProductionOrderNumber(), simalScheduleId);
            }
        } catch (Exception e) {
            logger.error("Error creating control orders from SimAL schedule {}: {}", simalScheduleId, e.getMessage(), e);
            throw new RuntimeException("Failed to create control orders: " + e.getMessage());
        }
    }

    /**
     * Map SimAL status to production order status
     */
    private String mapSimalStatusToPOStatus(String simalStatus) {
        return switch (simalStatus) {
            case "SCHEDULED" -> "SCHEDULED";
            case "IN_PROGRESS" -> "IN_PRODUCTION";
            case "COMPLETED" -> "COMPLETED";
            case "FAILED", "CANCELLED" -> "CANCELLED";
            default -> "SCHEDULED";
        };
    }

    /**
     * Inner class for SimAL Production Order Request
     */
    public static class SimalProductionOrderRequest {
        private String productionOrderNumber;
        private Long sourceCustomerOrderId;
        private LocalDateTime dueDate;
        private String priority;
        private String notes;

        // Getters and Setters
        public String getProductionOrderNumber() { return productionOrderNumber; }
        public void setProductionOrderNumber(String productionOrderNumber) { this.productionOrderNumber = productionOrderNumber; }

        public Long getSourceCustomerOrderId() { return sourceCustomerOrderId; }
        public void setSourceCustomerOrderId(Long sourceCustomerOrderId) { this.sourceCustomerOrderId = sourceCustomerOrderId; }

        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
