package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.FocusSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 专注会话Mapper
 */
@SuppressWarnings("SqlResolve")
@Mapper
public interface FocusSessionMapper extends BaseMapper<FocusSession> {
    
    /**
     * 根据用户ID和状态查找专注会话
     */
    @Select("SELECT * FROM `focussession` WHERE `user_id` = #{userId} AND `status` = #{status} ORDER BY `start_time` DESC LIMIT 1")
    FocusSession findLatestByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * 根据用户ID查找所有专注会话
     */
    @Select("SELECT * FROM `focussession` WHERE `user_id` = #{userId} ORDER BY `start_time` DESC")
    List<FocusSession> findByUserId(@Param("userId") Long userId);
}

