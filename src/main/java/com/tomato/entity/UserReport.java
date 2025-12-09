package com.tomato.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户报告实体
 */
@Data
@TableName("studyreport")
public class UserReport {
    @TableId(value = "report_id", type = IdType.AUTO)
    private Long reportId;

    @TableField("user_id")
    private Long userId;

    @TableField("report_type")
    private String reportType;

    @TableField("report_date")
    private java.time.LocalDate reportDate;

    @TableField("total_focus_time")
    private Integer totalFocusTime;

    @TableField("completed_tasks")
    private Integer completedTasks;

    @TableField("avg_daily_duration")
    private Float avgDailyDuration;

    @TableField("created_at")
    private LocalDateTime createdAt;
}

