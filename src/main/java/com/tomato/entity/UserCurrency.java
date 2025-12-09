package com.tomato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户货币实体
 */
@Data
@TableName("usercurrency")
public class UserCurrency {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("coins")
    private Integer coins;

    @TableField("check_day")
    private Integer checkDay;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

