package com.tomato.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自习室实体
 */
@Data
@TableName("room")
public class Room {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    private Long roomId;

    @TableField("room_name")
    private String roomName;

    @TableField("create_person")
    private Long createPerson;

    @TableField("max_members")
    private Integer maxMembers;

    @TableField("end_time")
    private Long endTime;

    @TableField("music_id")
    private Long musicId;

    @TableField("music_name")
    private String musicName;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
