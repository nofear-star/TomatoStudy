package com.tomato.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码（如果提供，则更新密码）
     */
    @Size(min = 6, max = 128, message = "密码长度必须在6-128个字符之间")
    private String password;
    
    /**
     * 性别
     */
    private String sex;
    
    /**
     * 生日（时间戳，毫秒）
     */
    private Long birthday;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
}

