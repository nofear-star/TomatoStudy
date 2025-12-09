package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long user_id;
    private String username;
    private String status;
    private String email;
    private String phone;
    private String sex;
    private Long birthday; // 时间戳（毫秒）
    private Integer tomato;
    private String province;
    private String avatar; // 头像URL
}

