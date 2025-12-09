package com.tomato.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StartFocusRequest {
    @NotBlank(message = "任务名称不能为空")
    private String task_name;
}

