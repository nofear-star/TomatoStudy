package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper
 */
public interface UserMapper extends BaseMapper<User> {
    
    // 只检查未删除的用户
    @Select("SELECT COUNT(*) > 0 FROM `users` WHERE username = #{username} AND (deleted IS NULL OR deleted = 0)")
    boolean existsByUsername(@Param("username") String username);
    
    @Select("SELECT COUNT(*) > 0 FROM `users` WHERE email = #{email} AND (deleted IS NULL OR deleted = 0)")
    boolean existsByEmail(@Param("email") String email);
    
    @Select("SELECT COUNT(*) > 0 FROM `users` WHERE phone = #{phone} AND phone IS NOT NULL AND phone != '' AND (deleted IS NULL OR deleted = 0)")
    boolean existsByPhone(@Param("phone") String phone);
    
    // 只查询未删除的用户
    @Select("SELECT * FROM `users` WHERE username = #{username} AND (deleted IS NULL OR deleted = 0) LIMIT 1")
    User findByUsername(@Param("username") String username);
    
    @Select("SELECT * FROM `users` WHERE user_id = #{userId} AND (deleted IS NULL OR deleted = 0) LIMIT 1")
    User findByUserId(@Param("userId") Long userId);
    
    @Select("SELECT * FROM `users` WHERE email = #{email} AND (deleted IS NULL OR deleted = 0) LIMIT 1")
    User findByEmail(@Param("email") String email);
}

