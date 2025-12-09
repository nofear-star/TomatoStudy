package com.tomato.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    private String task_name;
    
    private String task_note;
    
    @Positive(message = "计划时长必须大于0")
    private Integer duration; // 计划时长（单位：分钟）
}

