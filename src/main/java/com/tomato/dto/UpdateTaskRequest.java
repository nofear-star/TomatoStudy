package com.tomato.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    @NotNull(message = "任务ID不能为空")
    @JsonAlias({"taskId", "task_id"})
    private Long task_id;

    private String task_name;
    
    private String task_note;
    
    @Positive(message = "计划时长必须大于0")
    private Integer duration; // 计划时长（单位：分钟）

    /**
     * 任务状态：如 未完成 / 进行中 / 已完成
     */
    private String status;
}

