package com.tomato.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteTaskRequest {
    @NotNull(message = "任务ID不能为空")
    private Long task_id;
}

