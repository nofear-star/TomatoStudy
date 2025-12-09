package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.FriendRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 好友请求Mapper
 */
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
    
    @Select("SELECT * FROM friendrequest WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId} LIMIT 1")
    FriendRequest findByFromUserIdAndToUserId(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
    
    @Select("SELECT * FROM friendrequest WHERE to_user_id = #{toUserId} AND status = #{status}")
    List<FriendRequest> findByToUserIdAndStatus(@Param("toUserId") Long toUserId, @Param("status") String status);
    
    @Select("SELECT * FROM friendrequest WHERE from_user_id = #{fromUserId}")
    List<FriendRequest> findByFromUserId(@Param("fromUserId") Long fromUserId);
    
    @Select("SELECT COUNT(*) > 0 FROM friendrequest WHERE from_user_id = #{fromUserId} AND to_user_id = #{toUserId}")
    boolean existsByFromUserIdAndToUserId(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
}

