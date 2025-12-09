package com.tomato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 房间成员实体
 */
@Data
@TableName("roommember")
public class RoomMember {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    private Long roomId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private String role;

    @TableField("status")
    private String status;

    @TableField("joined_at")
    private LocalDateTime joinedAt;

    @TableField("session_focus_duration")
    private Integer sessionFocusDuration;
}

