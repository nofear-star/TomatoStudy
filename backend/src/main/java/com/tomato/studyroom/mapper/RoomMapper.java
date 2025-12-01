package com.tomato.studyroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.studyroom.entity.Room;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 自习室Mapper
 */
@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    List<Room> selectRoomsWithMemberCount();
}
