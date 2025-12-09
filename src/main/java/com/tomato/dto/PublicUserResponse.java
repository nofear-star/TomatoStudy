package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicUserResponse {
    private Long user_id;
    private String username;
    private String status;
    private String sex;
    private Long birthday;      // 根据隐私设置可能为 null
    private Integer tomato;
    private String province;    // 根据隐私设置可能为 null
}

