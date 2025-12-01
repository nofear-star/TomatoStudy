package com.tomato.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.studyroom.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
