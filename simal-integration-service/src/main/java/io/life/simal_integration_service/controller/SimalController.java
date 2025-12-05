package io.life.simal_integration_service.controller;

import io.life.simal_integration_service.dto.SimalProductionOrderRequest;
import io.life.simal_integration_service.dto.SimalScheduledOrderResponse;
import io.life.simal_integration_service.dto.SimalUpdateTimeRequest;
import io.life.simal_integration_service.service.ControlOrderIntegrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Mock SimAL Integration Controller.
 * Provides endpoints for production order scheduling and status updates.
 * Data is stored in-memory for demonstration purposes.
 */
@RestController
@RequestMapping("/api/simal")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class SimalController {

    // In-memory storage for scheduled orders
    private final Map<String, SimalScheduledOrderResponse> scheduledOrders = new HashMap<>();
    private final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private final ControlOrderIntegrationService controlOrderIntegrationService;

    public SimalController(ControlOrderIntegrationService controlOrderIntegrationService) {
        this.controlOrderIntegrationService = controlOrderIntegrationService;
    }

    /**
     * Mock endpoint to submit a production order for scheduling.
     * Generates a schedule with realistic task assignments.
     *
     * @param request Production order request
     * @return Scheduled order response with task assignments
     */
    @PostMapping("/production-order")
    public ResponseEntity<SimalScheduledOrderResponse> submitProductionOrder(
            @RequestBody SimalProductionOrderRequest request) {

        System.out.println("Received production order: " + request.getOrderNumber());

        // Generate schedule ID
        String scheduleId = "SCHED-" + System.currentTimeMillis();

        // Generate scheduled tasks based on order items
        List<SimalScheduledOrderResponse.ScheduledTask> tasks = generateScheduledTasks(request, scheduleId);

        // Calculate total duration
        int totalDuration = tasks.stream()
                .mapToInt(task -> task.getDuration() != null ? task.getDuration() : 0)
                .sum();

        // Build response
        SimalScheduledOrderResponse response = SimalScheduledOrderResponse.builder()
                .scheduleId(scheduleId)
                .orderNumber(request.getOrderNumber())
                .status("SCHEDULED")
                .estimatedCompletionTime(calculateCompletionTime(totalDuration))
                .scheduledTasks(tasks)
                .totalDuration(totalDuration)
                .build();

        // Store in-memory
        scheduledOrders.put(scheduleId, response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Mock endpoint to retrieve all scheduled orders.
     *
     * @return List of all scheduled orders
     */
    @GetMapping("/scheduled-orders")
    public ResponseEntity<List<SimalScheduledOrderResponse>> getScheduledOrders() {
        System.out.println("Fetching all scheduled orders. Total: " + scheduledOrders.size());
        return ResponseEntity.ok(new ArrayList<>(scheduledOrders.values()));
    }

    /**
     * Mock endpoint to retrieve a specific scheduled order by ID.
     *
     * @param scheduleId Schedule ID
     * @return Scheduled order or 404 if not found
     */
    @GetMapping("/scheduled-orders/{scheduleId}")
    public ResponseEntity<SimalScheduledOrderResponse> getScheduledOrder(
            @PathVariable String scheduleId) {
        System.out.println("Fetching schedule: " + scheduleId);

        SimalScheduledOrderResponse order = scheduledOrders.get(scheduleId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * Mock endpoint to update production time and status for a task.
     * Updates the in-memory schedule with actual execution time.
     *
     * @param request Update request with actual times and status
     * @return Updated scheduled order response
     */
    @PostMapping("/update-time")
    public ResponseEntity<SimalScheduledOrderResponse> updateProductionTime(
            @RequestBody SimalUpdateTimeRequest request) {

        System.out.println("Updating time for schedule: " + request.getScheduleId() + 
                         ", task: " + request.getTaskId());

        SimalScheduledOrderResponse order = scheduledOrders.get(request.getScheduleId());
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        // Find and update the task
        SimalScheduledOrderResponse.ScheduledTask taskToUpdate = order.getScheduledTasks()
                .stream()
                .filter(task -> task.getTaskId().equals(request.getTaskId()))
                .findFirst()
                .orElse(null);

        if (taskToUpdate == null) {
            return ResponseEntity.notFound().build();
        }

        // Update task with actual times
        if (request.getActualStartTime() != null) {
            taskToUpdate.setStartTime(request.getActualStartTime());
        }
        if (request.getActualEndTime() != null) {
            taskToUpdate.setEndTime(request.getActualEndTime());
        }
        if (request.getActualDuration() != null) {
            taskToUpdate.setDuration(request.getActualDuration());
        }
        if (request.getStatus() != null) {
            taskToUpdate.setStatus(request.getStatus());
        }

        // Update overall order status based on tasks
        updateOrderStatus(order);

        return ResponseEntity.ok(order);
    }

    /**
     * Mock endpoint to retrieve scheduled orders for a specific customer order.
     *
     * @param orderNumber Customer order number
     * @return Scheduled order or 404 if not found
     */
    @GetMapping("/scheduled-orders/order/{orderNumber}")
    public ResponseEntity<SimalScheduledOrderResponse> getScheduleByOrderNumber(
            @PathVariable String orderNumber) {
        System.out.println("Fetching schedule for order: " + orderNumber);

        SimalScheduledOrderResponse order = scheduledOrders.values()
                .stream()
                .filter(o -> o.getOrderNumber().equals(orderNumber))
                .findFirst()
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    /**
     * Helper method to generate scheduled tasks based on order items.
     */
    private List<SimalScheduledOrderResponse.ScheduledTask> generateScheduledTasks(
            SimalProductionOrderRequest request, String scheduleId) {

        List<SimalScheduledOrderResponse.ScheduledTask> tasks = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();
        int sequence = 1;

        if (request.getLineItems() != null) {
            for (SimalProductionOrderRequest.OrderLineItem item : request.getLineItems()) {
                int duration = item.getEstimatedDuration() != null ? item.getEstimatedDuration() : 30;

                String workstationType = item.getWorkstationType() != null ? 
                        item.getWorkstationType() : "MANUFACTURING";
                String workstationId = assignWorkstation(workstationType, sequence);

                SimalScheduledOrderResponse.ScheduledTask task = 
                        SimalScheduledOrderResponse.ScheduledTask.builder()
                        .taskId("TASK-" + scheduleId + "-" + sequence)
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .workstationId(workstationId)
                        .workstationName(getWorkstationName(workstationId))
                        .startTime(isoFormatter.format(currentTime))
                        .endTime(isoFormatter.format(currentTime.plusMinutes(duration)))
                        .duration(duration)
                        .status("PENDING")
                        .sequence(sequence)
                        .build();

                tasks.add(task);

                // Move to next time slot
                currentTime = currentTime.plusMinutes(duration + 5); // 5 min buffer
                sequence++;
            }
        }

        return tasks;
    }

    /**
     * Helper method to assign workstation based on type.
     */
    private String assignWorkstation(String workstationType, int sequenceNumber) {
        return switch (workstationType) {
            case "ASSEMBLY" -> "WS-" + (3 + (sequenceNumber % 2));
            case "WAREHOUSE" -> "WS-8";
            default -> "WS-" + (1 + (sequenceNumber % 2)); // MANUFACTURING
        };
    }

    /**
     * Helper method to get workstation name from ID.
     */
    private String getWorkstationName(String workstationId) {
        return switch (workstationId) {
            case "WS-1" -> "Manufacturing Bay 1";
            case "WS-2" -> "Manufacturing Bay 2";
            case "WS-3" -> "Assembly Line 1";
            case "WS-4" -> "Assembly Line 2";
            case "WS-8" -> "Modules Supermarket";
            default -> "Workstation " + workstationId;
        };
    }

    /**
     * Helper method to calculate estimated completion time.
     */
    private String calculateCompletionTime(int totalMinutes) {
        return isoFormatter.format(LocalDateTime.now().plusMinutes(totalMinutes));
    }

    /**
     * Helper method to update overall order status based on tasks.
     */
    private void updateOrderStatus(SimalScheduledOrderResponse order) {
        if (order.getScheduledTasks() == null || order.getScheduledTasks().isEmpty()) {
            return;
        }

        long completedTasks = order.getScheduledTasks().stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();

        long failedTasks = order.getScheduledTasks().stream()
                .filter(t -> "FAILED".equals(t.getStatus()))
                .count();

        long totalTasks = order.getScheduledTasks().size();

        if (failedTasks > 0) {
            order.setStatus("FAILED");
        } else if (completedTasks == totalTasks) {
            order.setStatus("COMPLETED");
        } else if (completedTasks > 0) {
            order.setStatus("IN_PROGRESS");
        } else {
            order.setStatus("SCHEDULED");
        }
    }

    /**
     * Create control orders from a scheduled order.
     * Processes the SimAL schedule and generates ProductionControlOrder and AssemblyControlOrder
     * entities in the order-processing-service.
     *
     * @param scheduleId The SimAL schedule ID
     * @param productionOrderId The production order ID in order-processing-service
     * @return Map of created control order numbers by workstation
     */
    @PostMapping("/scheduled-orders/{scheduleId}/create-control-orders")
    public ResponseEntity<Map<String, Object>> createControlOrders(
            @PathVariable String scheduleId,
            @RequestParam(required = false) Long productionOrderId) {

        System.out.println("Creating control orders from schedule: " + scheduleId);

        SimalScheduledOrderResponse schedule = scheduledOrders.get(scheduleId);
        if (schedule == null) {
            return ResponseEntity.notFound().build();
        }

        if (productionOrderId == null) {
            productionOrderId = 1L; // Default for testing
        }

        try {
            Map<String, String> createdOrders = controlOrderIntegrationService
                    .createControlOrdersFromSchedule(schedule, productionOrderId);

            Map<String, Object> response = new HashMap<>();
            response.put("scheduleId", scheduleId);
            response.put("productionOrderId", productionOrderId);
            response.put("controlOrdersCreated", createdOrders);
            response.put("totalControlOrders", createdOrders.size());
            response.put("status", "SUCCESS");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error creating control orders: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Batch create control orders for multiple schedules.
     * Useful for processing multiple production orders in one operation.
     *
     * @param request Batch request with list of schedule IDs and production order IDs
     * @return Results of all control order creations
     */
    @PostMapping("/create-control-orders/batch")
    public ResponseEntity<Map<String, Object>> createControlOrdersBatch(
            @RequestBody BatchControlOrderRequest request) {

        System.out.println("Creating control orders for batch: " + request.getScheduleIds().size() + " schedules");

        List<Map<String, Object>> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < request.getScheduleIds().size(); i++) {
            String scheduleId = request.getScheduleIds().get(i);
            Long productionOrderId = i < request.getProductionOrderIds().size() ?
                    request.getProductionOrderIds().get(i) : (long)(i + 1);

            SimalScheduledOrderResponse schedule = scheduledOrders.get(scheduleId);
            if (schedule == null) {
                failureCount++;
                results.add(Map.of("scheduleId", scheduleId, "status", "NOT_FOUND"));
                continue;
            }

            try {
                Map<String, String> createdOrders = controlOrderIntegrationService
                        .createControlOrdersFromSchedule(schedule, productionOrderId);

                successCount++;
                results.add(Map.of(
                        "scheduleId", scheduleId,
                        "productionOrderId", productionOrderId,
                        "controlOrders", createdOrders,
                        "status", "SUCCESS"
                ));
            } catch (Exception e) {
                failureCount++;
                results.add(Map.of(
                        "scheduleId", scheduleId,
                        "status", "ERROR",
                        "message", e.getMessage()
                ));
            }
        }

        Map<String, Object> batchResponse = new HashMap<>();
        batchResponse.put("totalRequests", request.getScheduleIds().size());
        batchResponse.put("successCount", successCount);
        batchResponse.put("failureCount", failureCount);
        batchResponse.put("results", results);

        return ResponseEntity.ok(batchResponse);
    }

    /**
     * DTO for batch control order creation request.
     */
    public static class BatchControlOrderRequest {
        public List<String> scheduleIds;
        public List<Long> productionOrderIds;

        public List<String> getScheduleIds() {
            return scheduleIds;
        }

        public void setScheduleIds(List<String> scheduleIds) {
            this.scheduleIds = scheduleIds;
        }

        public List<Long> getProductionOrderIds() {
            return productionOrderIds;
        }

        public void setProductionOrderIds(List<Long> productionOrderIds) {
            this.productionOrderIds = productionOrderIds;
        }
    }
}

