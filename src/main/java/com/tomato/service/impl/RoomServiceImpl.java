package com.tomato.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomato.dto.RoomCreateDTO;
import com.tomato.dto.RoomUpdateDTO;
import com.tomato.entity.BackgroundMusic;
import com.tomato.entity.Room;
import com.tomato.entity.RoomMember;
import com.tomato.entity.User;
import com.tomato.mapper.BackgroundMusicMapper;
import com.tomato.mapper.RoomMapper;
import com.tomato.mapper.RoomMemberMapper;
import com.tomato.mapper.UserMapper;
import com.tomato.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Autowired
    private BackgroundMusicMapper backgroundMusicMapper;

    @Override
    public List<Room> getRoomList() {
        return list();
    }

    @Override
    public Room getRoomById(Long roomId) {
        return findRoomByBusinessId(roomId);
    }

    @Override
    public Room getRoomByCode(String joinCode) {
        // 验证加入码格式：必须是6位数字
        if (joinCode == null || joinCode.length() != 6 || !joinCode.matches("\\d{6}")) {
            throw new IllegalArgumentException("加入码格式错误，必须是6位数字");
        }
        
        try {
            Long roomId = Long.parseLong(joinCode);
            return findRoomByBusinessId(roomId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("加入码格式错误，必须是6位数字");
        }
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
        // 处理背景音乐：根据musicName查询并设置musicId
        if (dto.getMusicName() != null && !dto.getMusicName().trim().isEmpty() && !"无".equals(dto.getMusicName())) {
            Long musicId = findMusicIdByName(dto.getMusicName());
            room.setMusicId(musicId);
            room.setMusicName(dto.getMusicName());
        } else {
            // 如果没有背景音乐或选择"无"，设置为null
            room.setMusicId(null);
            room.setMusicName(null);
        }
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        save(room);

        RoomMember member = new RoomMember();
        member.setRoomId(room.getRoomId());
        member.setUserId(room.getCreatePerson());
        member.setRole(MEMBER_ROLE_HOST);
        member.setStatus(MEMBER_STATUS_REST);
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
        // 处理背景音乐：根据musicName查询并设置musicId
        if (dto.getMusicName() != null) {
            if (dto.getMusicName().trim().isEmpty() || "无".equals(dto.getMusicName())) {
                // 如果设置为空或"无"，清除背景音乐
                room.setMusicId(null);
                room.setMusicName(null);
            } else {
                Long musicId = findMusicIdByName(dto.getMusicName());
                room.setMusicId(musicId);
                room.setMusicName(dto.getMusicName());
            }
        }
        room.setUpdatedAt(LocalDateTime.now());

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
        // 验证房间是否存在
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
            // 重新加入时，状态设置为休息中
            existMember.setStatus(MEMBER_STATUS_REST);
            existMember.setRole(MEMBER_ROLE_MEMBER);
            roomMemberMapper.updateById(existMember);
            return;
        }

        RoomMember member = new RoomMember();
        member.setRoomId(roomId);
        member.setUserId(userId);
        member.setRole(MEMBER_ROLE_MEMBER);
        member.setStatus(MEMBER_STATUS_REST); // 新成员加入时默认状态为休息中
        member.setSessionFocusDuration(0);
        roomMemberMapper.insert(member);
    }

    @Override
    @Transactional
    public void joinRoomByCode(String joinCode, Long userId) {
        // 通过加入码查找房间
        Room room = getRoomByCode(joinCode);
        // 调用通用的加入房间方法
        joinRoom(room.getRoomId(), userId);
    }

    @Override
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        LambdaQueryWrapper<RoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomMember::getRoomId, roomId)
               .eq(RoomMember::getUserId, userId);
        RoomMember member = roomMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new IllegalArgumentException("您不在此自习室中");
        }
        if (MEMBER_ROLE_HOST.equals(member.getRole())) {
            throw new IllegalArgumentException("房主无法直接离开，请解散自习室");
        }

        // 真正删除成员记录，从房间成员列表中移除
        roomMemberMapper.deleteById(member.getId());
    }

    @Override
    @Transactional
    public void leaveAllRooms(Long userId) {
        // 查找用户所在的所有房间
        List<RoomMember> members = roomMemberMapper.findAllByUserId(userId);
        if (members == null || members.isEmpty()) {
            return; // 用户不在任何房间中
        }

        // 移除用户从所有房间中（房主除外，房主需要手动解散房间）
        for (RoomMember member : members) {
            if (!MEMBER_ROLE_HOST.equals(member.getRole())) {
                roomMemberMapper.deleteById(member.getId());
            }
        }
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

    /**
     * 生成6位数字的房间ID（100000-999999）
     */
    private long generateBusinessId() {
        // 生成6位数字码：100000 到 999999
        long roomId;
        int maxAttempts = 100; // 最多尝试100次，避免无限循环
        int attempts = 0;
        
        do {
            // 生成100000到999999之间的随机数
            roomId = 100000L + ThreadLocalRandom.current().nextLong(900000L);
            attempts++;
            
            // 检查是否已存在，如果不存在则返回
            Room existingRoom = lambdaQuery().eq(Room::getRoomId, roomId).one();
            if (existingRoom == null) {
                return roomId;
            }
        } while (attempts < maxAttempts);
        
        // 如果100次尝试后仍然冲突，使用时间戳的后6位（确保唯一性）
        // 这种情况应该很少发生，但需要确保是6位数字
        String timestamp = String.valueOf(System.currentTimeMillis());
        if (timestamp.length() >= 6) {
            String lastSix = timestamp.substring(timestamp.length() - 6);
            long fallbackId = Long.parseLong(lastSix);
            // 确保在有效范围内（100000-999999）
            if (fallbackId < 100000) {
                fallbackId += 100000;
            } else if (fallbackId > 999999) {
                fallbackId = fallbackId % 900000 + 100000;
            }
            return fallbackId;
        }
        // 如果时间戳长度不足6位（理论上不会发生），返回一个默认值
        return 100000L + (System.currentTimeMillis() % 900000L);
    }

    private User findUserByBusinessId(Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserId, userId);
        return userMapper.selectOne(wrapper);
    }

    private boolean isActiveStatus(String status) {
        return MEMBER_STATUS_FOCUS.equals(status) || "active".equalsIgnoreCase(status);
    }

    /**
     * 根据音乐名称查找音乐ID
     * @param musicName 音乐名称
     * @return 音乐ID，如果未找到则返回null
     */
    private Long findMusicIdByName(String musicName) {
        if (musicName == null || musicName.trim().isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<BackgroundMusic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BackgroundMusic::getMusicName, musicName.trim());
        BackgroundMusic music = backgroundMusicMapper.selectOne(wrapper);
        return music != null ? music.getId() : null;
    }

    @Override
    @Transactional
    public void updateMemberStatus(Long roomId, Long userId, String status) {
        if (roomId == null || userId == null || status == null) {
            throw new IllegalArgumentException("roomId、userId、status 不能为空");
        }
        // 校验房间存在
        findRoomByBusinessId(roomId);
        LambdaQueryWrapper<RoomMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomMember::getRoomId, roomId).eq(RoomMember::getUserId, userId);
        RoomMember member = roomMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new IllegalArgumentException("成员不在房间中");
        }

        String normalized;
        String userStatus;
        switch (status.toLowerCase()) {
            case "focusing":
            case "focus":
            case "专注中":
            case "专注":
                normalized = MEMBER_STATUS_FOCUS;
                userStatus = "专注中";
                break;
            case "resting":
            case "rest":
            case "休息中":
            case "休息":
            default:
                normalized = MEMBER_STATUS_REST;
                userStatus = "在线";
                break;
        }
        
        // 更新房间成员状态
        member.setStatus(normalized);
        roomMemberMapper.updateById(member);
        
        // 同时更新用户表中的状态
        User user = userMapper.findByUserId(userId);
        if (user != null) {
            System.out.println("🔄 更新用户状态 - userId: " + userId + ", 旧状态: " + user.getStatus() + ", 新状态: " + userStatus);
            user.setStatus(userStatus);
            userMapper.updateById(user);
            System.out.println("✅ 用户状态已更新为: " + userStatus);
        } else {
            System.out.println("⚠️ 用户不存在，无法更新状态 - userId: " + userId);
        }
    }
}

