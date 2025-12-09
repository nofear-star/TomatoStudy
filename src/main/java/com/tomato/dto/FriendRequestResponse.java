package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestResponse {
    private Long id;
    private Long from_user_id;
    private String from_username;
    private Long to_user_id;
    private String to_username;
    private String message;
    private String status;
}

