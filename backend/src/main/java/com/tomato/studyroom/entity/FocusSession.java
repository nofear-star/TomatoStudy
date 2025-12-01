package com.tomato.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    @TableId(type = IdType.AUTO)
    private Long sessionId;
    private Long userId;
    private Long roomId;
    private String sessionType;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;
}
