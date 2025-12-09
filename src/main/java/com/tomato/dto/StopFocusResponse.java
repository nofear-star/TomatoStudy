package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StopFocusResponse {
    private String task_name;
    private String end_time;
    private Integer actual_duration; // 实际专注时长（分钟）
}

