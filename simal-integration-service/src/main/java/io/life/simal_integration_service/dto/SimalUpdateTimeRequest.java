package io.life.simal_integration_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating production time in SimAL.
 * Used to report actual vs. estimated production times.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimalUpdateTimeRequest {

    /**
     * Schedule ID to update.
     */
    private String scheduleId;

    /**
     * Task ID within the schedule to update.
     */
    private String taskId;

    /**
     * Actual start time (ISO 8601: YYYY-MM-DDTHH:mm:ss).
     */
    private String actualStartTime;

    /**
     * Actual end time (ISO 8601: YYYY-MM-DDTHH:mm:ss).
     */
    private String actualEndTime;

    /**
     * Actual duration in minutes.
     */
    private Integer actualDuration;

    /**
     * Updated task status: PENDING, IN_PROGRESS, COMPLETED, FAILED.
     */
    private String status;

    /**
     * Notes or comments about the task execution.
     */
    private String notes;

    /**
     * Percentage of task completion (0-100).
     */
    private Integer percentComplete;

    /**
     * Workstation ID where task was executed.
     */
    private String workstationId;
}
