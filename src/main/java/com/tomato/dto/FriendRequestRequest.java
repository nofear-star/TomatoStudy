package com.tomato.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestRequest {
    @NotBlank(message = "用户名不能为空")
    private String user_name; // 接收好友申请的用户名
    
    private String message; // 申请留言（可选）
}

