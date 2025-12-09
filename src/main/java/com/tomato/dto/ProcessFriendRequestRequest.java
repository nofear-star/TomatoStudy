package com.tomato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessFriendRequestRequest {
    @NotNull(message = "发送申请的用户ID不能为空")
    @Positive(message = "发送申请的用户ID必须大于0")
    private Long from_user_id; // 发送好友申请的用户ID
    
    @NotBlank(message = "操作不能为空")
    private String action; // 'accept' 或 'reject'
}

