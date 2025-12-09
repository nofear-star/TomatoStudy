package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FocusResponse {
    private String task_name;
    private String start_time; // 格式: "YYYY-MM-DD HH:mm:ss"
}

