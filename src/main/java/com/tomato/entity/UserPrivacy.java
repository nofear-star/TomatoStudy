package com.tomato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户隐私设置实体
 */
@Data
@TableName("userprivacy")
public class UserPrivacy {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("show_birthday")
    private String showBirthday;

    @TableField("show_study_time")
    private String showStudyTime;

    @TableField("show_location")
    private String showLocation;

    @TableField("allow_friend_request")
    private Integer allowFriendRequest;

    @TableField("searchable")
    private Integer searchable;

    @TableField("updated_at")
    private java.time.LocalDateTime updatedAt;
}

