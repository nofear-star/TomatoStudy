package com.tomato.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.studyroom.entity.RoomMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 房间成员Mapper
 */
@Mapper
public interface RoomMemberMapper extends BaseMapper<RoomMember> {
    List<RoomMember> selectMembersByRoomId(Long roomId);
    Integer countActiveMembersByRoomId(Long roomId);
}
