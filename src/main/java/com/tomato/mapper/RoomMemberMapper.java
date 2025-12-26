package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.RoomMember;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 房间成员Mapper
 */
public interface RoomMemberMapper extends BaseMapper<RoomMember> {
    List<RoomMember> selectMembersByRoomId(@Param("roomId") Long roomId);
    Integer countActiveMembersByRoomId(@Param("roomId") Long roomId);
    
    /**
     * 根据用户ID查找房间成员记录（用于获取room_id）
     */
    @Select("SELECT * FROM roommember WHERE user_id = #{userId} LIMIT 1")
    RoomMember findByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查找用户所在的所有房间成员记录
     */
    @Select("SELECT * FROM roommember WHERE user_id = #{userId}")
    List<RoomMember> findAllByUserId(@Param("userId") Long userId);
}

