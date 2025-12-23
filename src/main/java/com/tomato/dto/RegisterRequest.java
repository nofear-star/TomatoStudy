package com.tomato.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    // 手机号可选，如果为空字符串则跳过唯一性检查
    private String phone;

    @NotBlank
    @Size(min = 6, max = 128)
    private String password;
    
    // 验证码
    private String verificationCode;
}

