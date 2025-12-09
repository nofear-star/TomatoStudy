package com.tomato.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tomato.entity.Room;

import java.util.List;

/**
 * 自习室Mapper
 */
public interface RoomMapper extends BaseMapper<Room> {
    List<Room> selectRoomsWithMemberCount();
}

