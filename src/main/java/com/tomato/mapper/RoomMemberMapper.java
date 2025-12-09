package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.RoomMember;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 房间成员Mapper
 */
public interface RoomMemberMapper extends BaseMapper<RoomMember> {
    List<RoomMember> selectMembersByRoomId(@Param("roomId") Long roomId);
    Integer countActiveMembersByRoomId(@Param("roomId") Long roomId);
}

