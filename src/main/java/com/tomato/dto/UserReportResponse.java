package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReportResponse {
    private Long id;
    private Long user_id;
    private String report_type; // 'weekly', 'monthly'
    private String report_date;
    private String title;
    private Object summary; // JSON 对象
    private String share_image_url;
    private Integer is_read; // 0-未读，1-已读
    private String created_at; // 格式: "YYYY-MM-DD HH:mm:ss"
}

