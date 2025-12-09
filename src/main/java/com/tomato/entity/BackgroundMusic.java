package com.tomato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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

    @TableField("music_name")
    private String musicName;

    @TableField("audio_url")
    private String audioUrl;

    @TableField("price")
    private java.math.BigDecimal price;

    @TableField("is_free")
    private Integer isFree;

    @TableField("duration")
    private Integer duration;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

