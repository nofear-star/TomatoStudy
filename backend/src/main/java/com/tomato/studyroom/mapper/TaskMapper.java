package com.tomato.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.studyroom.entity.Task;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务Mapper
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
}
