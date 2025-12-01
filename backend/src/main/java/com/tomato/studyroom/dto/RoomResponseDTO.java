package com.tomato.studyroom.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自习室响应DTO
 */
@Data
public class RoomResponseDTO {
    private Long roomId;
    private String roomName;
    private Long createPerson;
    private Integer maxMembers;
    private Long endTime;      // 毫秒数
    private String musicName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

