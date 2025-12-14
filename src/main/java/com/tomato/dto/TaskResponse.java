package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Long task_id;
    private Long user_id;
    private String task_name;
    private String task_note;
    private Integer duration;
    private String status;
    private String created_at; // 格式: "YYYY-MM-DD HH:mm:ss"
}

