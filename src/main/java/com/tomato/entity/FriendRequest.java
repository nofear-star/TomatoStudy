package com.tomato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 好友请求实体
 */
@Data
@TableName("friendrequest")
public class FriendRequest {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("from_user_id")
    private Long fromUserId;

    @TableField("from_user_name")
    private String fromUserName;

    @TableField("to_user_id")
    private Long toUserId;

    @TableField("to_user_name")
    private String toUserName;

    @TableField("status")
    private String status;

    @TableField("message")
    private String message;
}

