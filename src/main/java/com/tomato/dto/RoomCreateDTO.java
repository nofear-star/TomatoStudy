package com.tomato.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建自习室DTO
 */
@Data
public class RoomCreateDTO {
    @NotBlank(message = "房间名称不能为空")
    private String roomName;

    @NotNull(message = "创建者不能为空")
    private Long createPerson;

    @NotNull(message = "最大人数不能为空")
    private Integer maxMembers;

    private Long endTime;
    private String musicName;
}

