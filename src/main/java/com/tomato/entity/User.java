package com.tomato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("status")
    private String status;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("sex")
    private String sex;

    @TableField("birthday")
    private LocalDate birthday;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("tomato")
    private Integer tomato;

    @TableField("province")
    private String province;

    @TableField("deleted")
    private Integer deleted; // 0-未删除，1-已删除（软删除）

    @TableField("avatar")
    private String avatar; // 头像URL
}

