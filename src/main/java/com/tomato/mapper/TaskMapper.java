package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.Task;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务Mapper
 */
public interface TaskMapper extends BaseMapper<Task> {
    
    @Select("SELECT * FROM tasks WHERE user_id = #{userId}")
    List<Task> findByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM tasks WHERE user_id = #{userId} AND status = #{status}")
    List<Task> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Select("SELECT * FROM tasks WHERE user_id = #{userId} AND task_name = #{taskName} LIMIT 1")
    Task findByUserIdAndTaskName(@Param("userId") Long userId, @Param("taskName") String taskName);
    
    @Select("SELECT * FROM tasks WHERE user_id = #{userId} AND status = #{status} LIMIT 1")
    Task findFirstByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Select("SELECT * FROM tasks WHERE task_id = #{taskId} AND user_id = #{userId} LIMIT 1")
    Task findByTaskIdAndUserId(@Param("taskId") Long taskId, @Param("userId") Long userId);
    
    @Select("SELECT * FROM tasks WHERE user_id = #{userId} AND status = '已完成' AND end_time >= #{startTime} AND end_time <= #{endTime}")
    List<Task> findCompletedTasksByUserIdAndTimeRange(@Param("userId") Long userId, 
                                                      @Param("startTime") LocalDateTime startTime, 
                                                      @Param("endTime") LocalDateTime endTime);
}

