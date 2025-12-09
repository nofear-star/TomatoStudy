package com.tomato.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePrivacyRequest {
    private String show_birthday; // 'public', 'friends', 'private'
    private String show_study_time; // 'public', 'friends', 'private'
    private String show_location; // 'public', 'friends', 'private'
    private Boolean allow_friend_request;
    private Boolean searchable;
}

