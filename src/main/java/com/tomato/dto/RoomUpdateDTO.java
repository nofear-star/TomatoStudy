package com.tomato.dto;

import lombok.Data;

/**
 * 更新自习室DTO
 */
@Data
public class RoomUpdateDTO {
    private Long roomId;
    private String roomName;
    private Long createPerson;
    private Integer maxMembers;
    private Long endTime;
    private String musicName;
}

