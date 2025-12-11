package com.tomato.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomato.dto.RoomCreateDTO;
import com.tomato.dto.RoomUpdateDTO;
import com.tomato.entity.Room;

import java.util.List;
import java.util.Map;

/**
 * 自习室服务接口
 */
public interface RoomService extends IService<Room> {
    List<Room> getRoomList();
    Room getRoomById(Long roomId);
    Room getRoomByCode(String joinCode);
    Room createRoom(RoomCreateDTO dto);
    Room updateRoom(Long roomId, RoomUpdateDTO dto);
    void deleteRoom(Long roomId, Long userId);
    void joinRoom(Long roomId, Long userId);
    void joinRoomByCode(String joinCode, Long userId);
    void leaveRoom(Long roomId, Long userId);
    List<Map<String, Object>> getRoomMembers(Long roomId);
    void kickMember(Long roomId, Long userId, Long targetUserId);
    void updateMemberStatus(Long roomId, Long userId, String status);
}

