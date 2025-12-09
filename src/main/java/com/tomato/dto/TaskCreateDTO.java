package com.tomato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建任务DTO
 */
@Data
public class TaskCreateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    private String taskNote;
    private Integer duration;
}

