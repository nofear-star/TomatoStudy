package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyResponse {
    private Long user_id;
    private Integer coins;
    private Integer check_day; // 本月签到天数
    private String updated_at; // 格式: "YYYY-MM-DD"
}

