package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FocusRecordResponse {
    private Long task_id;
    private String task_name;
    private String task_note;
    private Integer duration; // 计划时长（分钟）
    private Integer actual_duration; // 实际专注时长（分钟）
    private String status;
    private String start_time; // 格式: "YYYY-MM-DD HH:mm:ss"
    private String end_time; // 格式: "YYYY-MM-DD HH:mm:ss"
    private String created_at; // 格式: "YYYY-MM-DD HH:mm:ss"
}

