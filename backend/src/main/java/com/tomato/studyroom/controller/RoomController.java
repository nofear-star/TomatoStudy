package com.tomato.studyroom.controller;

import com.tomato.studyroom.common.Result;
import com.tomato.studyroom.dto.RoomCreateDTO;
import com.tomato.studyroom.dto.RoomResponseDTO;
import com.tomato.studyroom.dto.RoomUpdateDTO;
import com.tomato.studyroom.entity.Room;
import com.tomato.studyroom.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自习室控制器
 */
@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    /**
     * 获取自习室列表
     */
    @GetMapping
    public Result<List<RoomResponseDTO>> getRooms() {
        List<Room> rooms = roomService.getRoomList();
        List<RoomResponseDTO> responseList = rooms.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return Result.success(responseList);
    }

    /**
     * 创建自习室
     */
    @PostMapping
    public Result<RoomResponseDTO> createRoom(@Valid @RequestBody RoomCreateDTO dto) {
        Room room = roomService.createRoom(dto);
        return Result.success(convertToResponseDTO(room));
    }

    /**
     * 获取单个自习室
     */
    @GetMapping("/{roomId}")
    public Result<RoomResponseDTO> getRoom(@PathVariable Long roomId) {
        Room room = roomService.getRoomById(roomId);
        return Result.success(convertToResponseDTO(room));
    }

    /**
     * 更新自习室
     */
    @PutMapping("/{roomId}")
    public Result<RoomResponseDTO> updateRoom(@PathVariable Long roomId, @RequestBody RoomUpdateDTO dto) {
        Room room = roomService.updateRoom(roomId, dto);
        return Result.success(convertToResponseDTO(room));
    }

    /**
     * 解散自习室(限创建者)
     */
    @DeleteMapping("/{roomId}")
    public Result<?> deleteRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        roomService.deleteRoom(roomId, userId);
        return Result.success("解散成功", null);
    }

    /**
     * 加入自习室
     */
    @PostMapping("/{roomId}/join")
    public Result<?> joinRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        roomService.joinRoom(roomId, userId);
        return Result.success("加入成功", null);
    }

    /**
     * 离开自习室
     */
    @PostMapping("/{roomId}/leave")
    public Result<?> leaveRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        roomService.leaveRoom(roomId, userId);
        return Result.success("离开成功", null);
    }

    /**
     * 获取自习室成员列表
     */
    @GetMapping("/{roomId}/members")
    public Result<List<Map<String, Object>>> getRoomMembers(@PathVariable Long roomId) {
        List<Map<String, Object>> members = roomService.getRoomMembers(roomId);
        return Result.success(members);
    }

    /**
     * 踢出成员
     */
    @DeleteMapping("/{roomId}/members/{userId}")
    public Result<?> kickMember(@PathVariable Long roomId, 
                                @PathVariable Long userId,
                                @RequestParam Long creatorId) {
        roomService.kickMember(roomId, creatorId, userId);
        return Result.success("踢出成功", null);
    }

    /**
     * 将 Room 实体转换为 RoomResponseDTO
     */
    private RoomResponseDTO convertToResponseDTO(Room room) {
        RoomResponseDTO dto = new RoomResponseDTO();
        BeanUtils.copyProperties(room, dto);
        return dto;
    }
}
