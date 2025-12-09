package com.tomato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    @NotBlank(message = "任务名称不能为空")
    private String task_name;
    
    private String task_note;
    
    @NotNull(message = "计划时长不能为空")
    @Positive(message = "计划时长必须大于0")
    private Integer duration; // 计划时长（单位：分钟）
}

