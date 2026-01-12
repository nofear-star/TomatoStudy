package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendResponse {
    private Long friend_id;
    private String friend_username;
    private String friend_status;
    private String birthday;
    private String province;
    private String avatar;
}

