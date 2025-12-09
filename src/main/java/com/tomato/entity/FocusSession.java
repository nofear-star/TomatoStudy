package com.tomato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专注会话实体
 */
@Data
@TableName("focussession")
public class FocusSession {
    @TableId(value = "session_id", type = IdType.AUTO)
    private Long sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("room_id")
    private Long roomId;

    @TableField("task_id")
    private Long taskId;

    @TableField("session_type")
    private String sessionType;

    @TableField("duration")
    private Integer duration;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

