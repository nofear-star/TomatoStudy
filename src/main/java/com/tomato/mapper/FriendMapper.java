package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.Friend;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友Mapper
 */
public interface FriendMapper extends BaseMapper<Friend> {
    
    @Select("SELECT * FROM friend WHERE user_id = #{userId} AND friend_id = #{friendId} LIMIT 1")
    Friend findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);
    
    @Select("SELECT COUNT(*) > 0 FROM friend WHERE user_id = #{userId} AND friend_id = #{friendId}")
    boolean existsByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);
    
    @Select("SELECT * FROM friend WHERE user_id = #{userId}")
    List<Friend> findByUserId(@Param("userId") Long userId);
}

