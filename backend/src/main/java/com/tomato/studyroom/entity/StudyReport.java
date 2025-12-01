package com.tomato.studyroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习报告实体
 */
@Data
@TableName("studyreport")
public class StudyReport {
    @TableId(type = IdType.AUTO)
    private Long reportId;
    private Long userId;
    private LocalDate reportDate;
    private Integer totalStudyTime;
    private Integer completedSessions;
    private LocalDateTime createdAt;
}
