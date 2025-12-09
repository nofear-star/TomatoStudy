package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.UserReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户报告Mapper
 */
public interface UserReportMapper extends BaseMapper<UserReport> {
    
    @Select("SELECT * FROM studyreport WHERE user_id = #{userId}")
    List<UserReport> findByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM studyreport WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<UserReport> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Select("SELECT * FROM studyreport WHERE user_id = #{userId} AND report_type = #{reportType} ORDER BY created_at DESC")
    List<UserReport> findByUserIdAndReportTypeOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("reportType") String reportType);
}

