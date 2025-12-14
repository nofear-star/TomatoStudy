package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.CheckInRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到记录Mapper
 */
public interface CheckInRecordMapper extends BaseMapper<CheckInRecord> {
    
    /**
     * 查询用户指定日期是否已签到
     */
    @Select("SELECT * FROM checkin_records WHERE user_id = #{userId} AND checkin_date = #{checkinDate} LIMIT 1")
    CheckInRecord findByUserIdAndDate(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);
    
    /**
     * 统计用户本月签到天数
     */
    @Select("SELECT COUNT(*) FROM checkin_records WHERE user_id = #{userId} AND YEAR(checkin_date) = #{year} AND MONTH(checkin_date) = #{month}")
    Integer countCheckInDaysByMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
    
    /**
     * 获取用户本月所有签到日期
     */
    @Select("SELECT checkin_date FROM checkin_records WHERE user_id = #{userId} AND YEAR(checkin_date) = #{year} AND MONTH(checkin_date) = #{month} ORDER BY checkin_date ASC")
    List<LocalDate> findCheckInDatesByMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
}

