package com.tomato.studyroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomato.studyroom.dto.RoomCreateDTO;
import com.tomato.studyroom.dto.RoomUpdateDTO;
import com.tomato.studyroom.entity.Room;
import com.tomato.studyroom.entity.RoomMember;
import com.tomato.studyroom.entity.User;
import com.tomato.studyroom.mapper.RoomMapper;
import com.tomato.studyroom.mapper.RoomMemberMapper;
import com.tomato.studyroom.mapper.UserMapper;
import com.tomato.studyroom.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    private static final String MEMBER_STATUS_FOCUS = "专注中";
    private static final String MEMBER_STATUS_REST = "休息中";
    private static final String MEMBER_ROLE_HOST = "房主";
    private static final String MEMBER_ROLE_MEMBER = "成员";

    @Autowired
    private RoomMemberMapper roomMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Room> getRoomList() {
        return list();
    }

    @Override
    public Room getRoomById(Long roomId) {
        return findRoomByBusinessId(roomId);
    }

    @Override
    @Transactional
    public Room createRoom(RoomCreateDTO dto) {
        Room room = new Room();
        BeanUtils.copyProperties(dto, room);
        room.setRoomId(generateBusinessId());
        room.setCreatePerson(dto.getCreatePerson());
        room.setMaxMembers(dto.getMaxMembers());
        if (dto.getEndTime() != null) {
            room.setEndTime(dto.getEndTime());
        }
        save(room);

        RoomMember member = new RoomMember();
        member.setRoomId(room.getRoomId());
        member.setUserId(room.getCreatePerson());
        member.setRole(MEMBER_ROLE_HOST);
        member.setStatus(MEMBER_STATUS_FOCUS);
        member.setSessionFocusDuration(0);
        roomMemberMapper.insert(member);

        return room;
    }

    @Override
    @Transactional
    public Room updateRoom(Long roomId, RoomUpdateDTO dto) {
        Room room = findRoomByBusinessId(roomId);

        if (dto.getRoomName() != null) room.setRoomName(dto.getRoomName());
        if (dto.getMaxMembers() != null) room.setMaxMembers(dto.getMaxMembers());
        if (dto.getEndTime() != null) {
            room.setEndTime(dto.getEndTime());
        }
        if (dto.getMusicName() != null) room.setMusicName(dto.getMusicName());

        updateById(room);
        return room;
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId, Long userId) {
        Room room = findRoomByBusinessId(roomId);
        if (!room.getCreatePerson().equals(userId)) {
            throw new IllegalArgumentException("只有创建者可以解散自习室");
        }

        LambdaQueryWrapper<RoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomMember::getRoomId, room.getRoomId());
        roomMemberMapper.delete(wrapper);

        removeById(room.getId());
    }

    @Override
    @Transactional
    public void joinRoom(Long roomId, Long userId) {
        Room room = findRoomByBusinessId(roomId);

        Integer currentCount = roomMemberMapper.countActiveMembersByRoomId(roomId);
        if (currentCount >= room.getMaxMembers()) {
            throw new IllegalArgumentException("自习室已满");
        }

        LambdaQueryWrapper<RoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomMember::getRoomId, roomId)
               .eq(RoomMember::getUserId, userId);
        RoomMember existMember = roomMemberMapper.selectOne(wrapper);
        if (existMember != null) {
            if (isActiveStatus(existMember.getStatus())) {
                throw new IllegalArgumentException("您已在此自习室中");
            }
            existMember.setStatus(MEMBER_STATUS_FOCUS);
            existMember.setRole(MEMBER_ROLE_MEMBER);
            roomMemberMapper.updateById(existMember);
            return;
        }

        RoomMember member = new RoomMember();
        member.setRoomId(roomId);
        member.setUserId(userId);
        member.setRole(MEMBER_ROLE_MEMBER);
        member.setStatus(MEMBER_STATUS_FOCUS);
        member.setSessionFocusDuration(0);
        roomMemberMapper.insert(member);
    }

    @Override
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        LambdaQueryWrapper<RoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomMember::getRoomId, roomId)
               .eq(RoomMember::getUserId, userId)
               .in(RoomMember::getStatus, MEMBER_STATUS_FOCUS, "active");
        RoomMember member = roomMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new IllegalArgumentException("您不在此自习室中");
        }
        if (MEMBER_ROLE_HOST.equals(member.getRole())) {
            throw new IllegalArgumentException("房主无法直接离开，请解散自习室");
        }

        member.setStatus(MEMBER_STATUS_REST);
        roomMemberMapper.updateById(member);
    }

    @Override
    public List<Map<String, Object>> getRoomMembers(Long roomId) {
        List<RoomMember> members = roomMemberMapper.selectMembersByRoomId(roomId);
        return members.stream()
                .map(member -> {
                    User user = findUserByBusinessId(member.getUserId());
                    Map<String, Object> memberMap = new HashMap<>();
                    memberMap.put("userId", member.getUserId());
                    memberMap.put("username", user != null ? user.getUsername() : "");
                    memberMap.put("avatar", "");
                    memberMap.put("joinedAt", member.getJoinedAt());
                    memberMap.put("role", member.getRole());
                    memberMap.put("status", member.getStatus());
                    return memberMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void kickMember(Long roomId, Long userId, Long targetUserId) {
        Room room = findRoomByBusinessId(roomId);
        if (!room.getCreatePerson().equals(userId)) {
            throw new IllegalArgumentException("只有房主可以踢出成员");
        }
        if (room.getCreatePerson().equals(targetUserId)) {
            throw new IllegalArgumentException("不能踢出房主");
        }

        LambdaQueryWrapper<RoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomMember::getRoomId, roomId)
               .eq(RoomMember::getUserId, targetUserId);
        RoomMember member = roomMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new IllegalArgumentException("该用户不在房间中");
        }

        roomMemberMapper.deleteById(member.getId());
    }

    private Room findRoomByBusinessId(Long roomId) {
        Room room = lambdaQuery().eq(Room::getRoomId, roomId).one();
        if (room == null) {
            throw new IllegalArgumentException("自习室不存在");
        }
        return room;
    }

    private long generateBusinessId() {
        return System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000);
    }

    private User findUserByBusinessId(Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserId, userId);
        return userMapper.selectOne(wrapper);
    }

    private boolean isActiveStatus(String status) {
        return MEMBER_STATUS_FOCUS.equals(status) || "active".equalsIgnoreCase(status);
    }
}
