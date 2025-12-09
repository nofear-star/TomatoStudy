package com.tomato.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新任务DTO
 */
@Data
public class TaskUpdateDTO {
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String taskName;
    private String taskNote;
    private Integer duration;
    private String status; // 未完成, 进行中, 已完成
}

