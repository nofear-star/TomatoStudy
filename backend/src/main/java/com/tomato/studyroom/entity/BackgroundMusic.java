package com.tomato.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 背景音乐实体
 */
@Data
@TableName("backgroundmusic")
public class BackgroundMusic {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String musicName;
    private String audioUrl;
    private Integer duration;
    private LocalDateTime createdAt;
}
