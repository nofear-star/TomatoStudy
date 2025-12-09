package com.tomato.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFriendRequest {
    @NotBlank(message = "好友用户名不能为空")
    private String friend_name; // 要删除的好友用户名
}

