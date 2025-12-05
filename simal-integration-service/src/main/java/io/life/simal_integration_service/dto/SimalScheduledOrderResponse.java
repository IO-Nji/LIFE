package io.life.simal_integration_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for scheduled orders response from SimAL.
 * Contains the scheduled timeline and workstation assignments.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimalScheduledOrderResponse {

    /**
     * Reference ID for the scheduled order in SimAL.
     */
    private String scheduleId;

    /**
     * Original order number from customer order system.
     */
    private String orderNumber;

    /**
     * Overall schedule status: SCHEDULED, IN_PROGRESS, COMPLETED, FAILED.
     */
    private String status;

    /**
     * Overall estimated completion time (ISO 8601 format: YYYY-MM-DDTHH:mm:ss).
     */
    private String estimatedCompletionTime;

    /**
     * List of scheduled tasks with workstation assignments.
     */
    private List<ScheduledTask> scheduledTasks;

    /**
     * Total estimated duration in minutes for entire production.
     */
    private Integer totalDuration;

    /**
     * Scheduled task with workstation assignment.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduledTask {

        /**
         * Task identifier within this schedule.
         */
        private String taskId;

        /**
         * Item being produced.
         */
        private String itemId;

        /**
         * Item name for reference.
         */
        private String itemName;

        /**
         * Quantity for this task.
         */
        private Integer quantity;

        /**
         * Assigned workstation ID.
         */
        private String workstationId;

        /**
         * Workstation name.
         */
        private String workstationName;

        /**
         * Scheduled start time (ISO 8601: YYYY-MM-DDTHH:mm:ss).
         */
        private String startTime;

        /**
         * Scheduled end time (ISO 8601: YYYY-MM-DDTHH:mm:ss).
         */
        private String endTime;

        /**
         * Duration in minutes.
         */
        private Integer duration;

        /**
         * Task status: PENDING, IN_PROGRESS, COMPLETED, FAILED.
         */
        private String status;

        /**
         * Sequence/order of execution.
         */
        private Integer sequence;
    }
}
