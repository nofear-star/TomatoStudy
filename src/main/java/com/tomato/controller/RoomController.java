package com.tomato.controller;

import com.tomato.common.ApiResponse;
import com.tomato.dto.RoomCreateDTO;
import com.tomato.dto.RoomResponseDTO;
import com.tomato.dto.RoomUpdateDTO;
import com.tomato.entity.Room;
import com.tomato.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
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

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * 获取自习室列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponseDTO>>> getRooms() {
        List<Room> rooms = roomService.getRoomList();
        List<RoomResponseDTO> responseList = rooms.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responseList));
    }

    /**
     * 创建自习室
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> createRoom(@Valid @RequestBody RoomCreateDTO dto) {
        Room room = roomService.createRoom(dto);
        return ResponseEntity.ok(ApiResponse.success(convertToResponseDTO(room)));
    }

    /**
     * 获取单个自习室
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> getRoom(@PathVariable Long roomId) {
        Room room = roomService.getRoomById(roomId);
        return ResponseEntity.ok(ApiResponse.success(convertToResponseDTO(room)));
    }

    /**
     * 更新自习室
     */
    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> updateRoom(@PathVariable Long roomId, @RequestBody RoomUpdateDTO dto) {
        Room room = roomService.updateRoom(roomId, dto);
        return ResponseEntity.ok(ApiResponse.success(convertToResponseDTO(room)));
    }

    /**
     * 解散自习室(限创建者)
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        roomService.deleteRoom(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("解散成功", null));
    }

    /**
     * 加入自习室（通过房间ID）
     */
    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<Void>> joinRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        roomService.joinRoom(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("加入成功", null));
    }

    /**
     * 通过加入码加入自习室
     */
    @PostMapping("/join-by-code")
    public ResponseEntity<ApiResponse<RoomResponseDTO>> joinRoomByCode(
            @RequestParam String joinCode, 
            @RequestParam Long userId) {
        Room room = roomService.getRoomByCode(joinCode);
        roomService.joinRoomByCode(joinCode, userId);
        return ResponseEntity.ok(ApiResponse.success("加入成功", convertToResponseDTO(room)));
    }

    /**
     * 离开自习室
     */
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        roomService.leaveRoom(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("离开成功", null));
    }

    /**
     * 获取自习室成员列表
     */
    @GetMapping("/{roomId}/members")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRoomMembers(@PathVariable Long roomId) {
        List<Map<String, Object>> members = roomService.getRoomMembers(roomId);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    /**
     * 踢出成员
     */
    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> kickMember(@PathVariable Long roomId, 
                                @PathVariable Long userId,
                                @RequestParam Long creatorId) {
        roomService.kickMember(roomId, creatorId, userId);
        return ResponseEntity.ok(ApiResponse.success("踢出成功", null));
    }

    /**
     * 将 Room 实体转换为 RoomResponseDTO
     */
    private RoomResponseDTO convertToResponseDTO(Room room) {
        RoomResponseDTO dto = new RoomResponseDTO();
        BeanUtils.copyProperties(room, dto);
        return dto;
    }

    @PutMapping("/{roomId}/status")
    public ResponseEntity<ApiResponse<Void>> updateMemberStatus(@PathVariable Long roomId,
                                                                @RequestBody Map<String, Object> payload) {
        Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
        String status = payload.get("status") != null ? payload.get("status").toString() : null;
        roomService.updateMemberStatus(roomId, userId, status);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", null));
    }
}

