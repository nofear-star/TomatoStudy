package com.tomato.service;

import com.tomato.common.ApiResponse;
import com.tomato.dto.*;
import com.tomato.entity.*;
import com.tomato.mapper.*;
import com.tomato.security.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserPrivacyMapper userPrivacyMapper;
    private final UserCurrencyMapper userCurrencyMapper;
    private final TaskMapper taskMapper;
    private final UserReportMapper userReportMapper;
    private final FriendRequestMapper friendRequestMapper;
    private final FriendMapper friendMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final FocusSessionMapper focusSessionMapper;
    private final RoomMemberMapper roomMemberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final FileUploadService fileUploadService;

    public UserService(UserMapper userMapper, 
                       UserPrivacyMapper userPrivacyMapper,
                       UserCurrencyMapper userCurrencyMapper,
                       TaskMapper taskMapper,
                       UserReportMapper userReportMapper,
                       FriendRequestMapper friendRequestMapper,
                       FriendMapper friendMapper,
                       CheckInRecordMapper checkInRecordMapper,
                       FocusSessionMapper focusSessionMapper,
                       RoomMemberMapper roomMemberMapper,
                       PasswordEncoder passwordEncoder, 
                       JwtUtil jwtUtil,
                       ObjectMapper objectMapper,
                       FileUploadService fileUploadService) {
        this.userMapper = userMapper;
        this.userPrivacyMapper = userPrivacyMapper;
        this.userCurrencyMapper = userCurrencyMapper;
        this.taskMapper = taskMapper;
        this.userReportMapper = userReportMapper;
        this.friendRequestMapper = friendRequestMapper;
        this.friendMapper = friendMapper;
        this.checkInRecordMapper = checkInRecordMapper;
        this.focusSessionMapper = focusSessionMapper;
        this.roomMemberMapper = roomMemberMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.fileUploadService = fileUploadService;
    }

    /**
     * 从 token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前用户信息
     * 如果用户状态是"离线"，自动更新为"在线"（因为用户正在调用API，说明用户在线）
     * 如果用户状态是"专注中"或"在线"，保持不变
     */
    @Transactional
    public ApiResponse<UserResponse> getCurrentUser(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        // 如果用户状态是"离线"，自动更新为"在线"（因为用户正在调用API，说明用户在线）
        // 如果用户状态是"专注中"或"在线"，保持不变
        String currentStatus = user.getStatus();
        if (currentStatus == null || "离线".equals(currentStatus)) {
            user.setStatus("在线");
            userMapper.updateById(user);
        }

        UserResponse userResponse = convertToUserResponse(user);
        
        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("获取成功")
                .data(userResponse)
                .build();
    }

    /**
     * 设置用户状态为离线（用于页面关闭时调用）
     */
    @Transactional
    public ApiResponse<Void> setUserOffline(String token) {
        Long userId = getUserIdFromToken(token);
        return setUserOfflineByUserId(userId);
    }

    /**
     * 根据 userId 将用户状态设置为离线
     * 这样即使多窗口/多 token，也可以精确更新指定用户
     */
    @Transactional
    public ApiResponse<Void> setUserOfflineByUserId(Long userId) {
        if (userId == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("无效的用户ID")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        user.setStatus("离线");
        userMapper.updateById(user);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("状态已更新为离线")
                .build();
    }

    /**
     * 更新当前用户信息
     */
    @Transactional
    public ApiResponse<UserResponse> updateCurrentUser(String token, UpdateUserRequest req) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        // 更新字段（只更新提供的字段）
        
        // 更新用户名（需要检查唯一性）
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            // 检查用户名是否已被其他用户使用
            if (!user.getUsername().equals(req.getUsername()) && 
                userMapper.existsByUsername(req.getUsername())) {
                return ApiResponse.<UserResponse>builder()
                        .success(false)
                        .message("用户名已被使用")
                        .build();
            }
            user.setUsername(req.getUsername());
        }
        
        // 更新密码
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        
        // 更新性别
        if (req.getSex() != null && !req.getSex().isBlank()) {
            user.setSex(req.getSex());
        }

        // 更新生日
        if (req.getBirthday() != null) {
            LocalDate birthday = Instant.ofEpochMilli(req.getBirthday())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            user.setBirthday(birthday);
        }

        // 更新省份
        if (req.getProvince() != null && !req.getProvince().isBlank()) {
            user.setProvince(req.getProvince());
        }
        
        // 更新邮箱（需要检查唯一性）
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            // 检查邮箱是否已被其他用户使用
            if (!user.getEmail().equals(req.getEmail()) && 
                userMapper.existsByEmail(req.getEmail())) {
                return ApiResponse.<UserResponse>builder()
                        .success(false)
                        .message("邮箱已被注册")
                        .build();
            }
            user.setEmail(req.getEmail());
        }
        
        // 更新手机号（需要检查唯一性）
        if (req.getPhone() != null) {
            String phone = req.getPhone().trim();
            // 如果手机号不为空，检查唯一性
            if (!phone.isEmpty()) {
                // 检查手机号是否已被其他用户使用
                if ((user.getPhone() == null || !user.getPhone().equals(phone)) && 
                    userMapper.existsByPhone(phone)) {
                    return ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message("手机号已被注册")
                            .build();
                }
                user.setPhone(phone);
            } else {
                // 如果手机号为空字符串，设置为 null
                user.setPhone(null);
            }
        }

        // 注意：users表没有updated_at字段，所以不需要更新时间
        // 如果将来添加了updated_at字段，可以在这里设置：
        // user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        UserResponse userResponse = convertToUserResponse(user);

        return ApiResponse.<UserResponse>builder()
                .success(true)
                .message("更新成功")
                .data(userResponse)
                .build();
    }

    /**
     * 上传用户头像
     */
    @Transactional
    public ApiResponse<UserResponse> uploadAvatar(String token, MultipartFile file) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        try {
            // 删除旧头像（如果存在）
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                fileUploadService.deleteAvatar(user.getAvatar());
            }

            // 上传新头像
            String avatarUrl = fileUploadService.uploadAvatar(file, userId);
            
            // 更新用户头像URL
            user.setAvatar(avatarUrl);
            userMapper.updateById(user);

            // 返回更新后的用户信息
            UserResponse userResponse = convertToUserResponse(user);
            return ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("头像上传成功")
                    .data(userResponse)
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message("头像上传失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取当前用户隐私设置
     */
    public ApiResponse<PrivacyResponse> getCurrentUserPrivacy(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<PrivacyResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        UserPrivacy privacy = userPrivacyMapper.findByUserId(userId);
        if (privacy == null) {
            return ApiResponse.<PrivacyResponse>builder()
                    .success(false)
                    .message("隐私设置不存在")
                    .build();
        }

        PrivacyResponse privacyResponse = convertToPrivacyResponse(privacy);

        return ApiResponse.<PrivacyResponse>builder()
                .success(true)
                .message("获取成功")
                .data(privacyResponse)
                .build();
    }

    /**
     * 更新当前用户隐私设置
     */
    @Transactional
    public ApiResponse<PrivacyResponse> updateCurrentUserPrivacy(String token, UpdatePrivacyRequest req) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<PrivacyResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        UserPrivacy privacy = userPrivacyMapper.findByUserId(userId);
        if (privacy == null) {
            return ApiResponse.<PrivacyResponse>builder()
                    .success(false)
                    .message("隐私设置不存在")
                    .build();
        }

        // 更新字段（只更新提供的字段）
        if (req.getShow_birthday() != null) {
            privacy.setShowBirthday(req.getShow_birthday());
        }

        if (req.getShow_study_time() != null) {
            privacy.setShowStudyTime(req.getShow_study_time());
        }

        if (req.getShow_location() != null) {
            privacy.setShowLocation(req.getShow_location());
        }

        if (req.getAllow_friend_request() != null) {
            privacy.setAllowFriendRequest(req.getAllow_friend_request() ? 1 : 0);
        }

        if (req.getSearchable() != null) {
            privacy.setSearchable(req.getSearchable() ? 1 : 0);
        }

        // 更新更新时间
        privacy.setUpdatedAt(java.time.LocalDateTime.now());

        userPrivacyMapper.updateById(privacy);
        PrivacyResponse privacyResponse = convertToPrivacyResponse(privacy);

        return ApiResponse.<PrivacyResponse>builder()
                .success(true)
                .message("更新成功")
                .data(privacyResponse)
                .build();
    }

    /**
     * 获取当前用户资产信息
     */
    public ApiResponse<CurrencyResponse> getCurrentUserCurrency(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        UserCurrency currency = userCurrencyMapper.findByUserId(userId);
        if (currency == null) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message("资产信息不存在")
                    .build();
        }

        CurrencyResponse currencyResponse = convertToCurrencyResponse(currency, userId);

        return ApiResponse.<CurrencyResponse>builder()
                .success(true)
                .message("获取成功")
                .data(currencyResponse)
                .build();
    }

    /**
     * 获取用户本月所有签到日期
     */
    public ApiResponse<List<String>> getCurrentMonthCheckInDates(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<List<String>>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        List<LocalDate> checkInDates = checkInRecordMapper.findCheckInDatesByMonth(userId, year, month);
        
        // 转换为字符串列表（格式：YYYY-MM-DD）
        List<String> dateStrings = checkInDates.stream()
                .map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .collect(Collectors.toList());

        return ApiResponse.<List<String>>builder()
                .success(true)
                .message("获取成功")
                .data(dateStrings)
                .build();
    }

    /**
     * 每日签到
     * 功能：
     * 1. 检查今天是否已签到
     * 2. 如果未签到，创建签到记录
     * 3. 更新用户货币表（增加1个番茄，更新本月签到天数）
     * 4. 更新users表的tomato字段
     */
    @Transactional
    public ApiResponse<CurrencyResponse> dailyCheckIn(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        // 获取今天的日期
        LocalDate today = LocalDate.now();

        // 检查今天是否已签到
        CheckInRecord existingRecord = checkInRecordMapper.findByUserIdAndDate(userId, today);
        if (existingRecord != null) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message("今日已签到，请明天再来")
                    .build();
        }

        // 创建签到记录
        CheckInRecord checkInRecord = new CheckInRecord();
        checkInRecord.setUserId(userId);
        checkInRecord.setCheckinDate(today);
        checkInRecord.setCreatedAt(LocalDateTime.now());
        checkInRecordMapper.insert(checkInRecord);

        // 获取或创建用户货币记录
        UserCurrency currency = userCurrencyMapper.findByUserId(userId);
        if (currency == null) {
            // 如果不存在，创建新的货币记录
            currency = new UserCurrency();
            currency.setUserId(userId);
            currency.setCoins(0);
            currency.setCheckDay(0);
            currency.setUpdatedAt(LocalDateTime.now());
            userCurrencyMapper.insert(currency);
        }

        // 更新本月签到天数
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();
        Integer checkInDays = checkInRecordMapper.countCheckInDaysByMonth(userId, currentYear, currentMonth);
        currency.setCheckDay(checkInDays != null ? checkInDays : 1);
        currency.setUpdatedAt(LocalDateTime.now());
        userCurrencyMapper.updateById(currency);

        // 更新users表的tomato字段（增加1个番茄）
        User user = userMapper.findByUserId(userId);
        if (user != null) {
            int currentTomatoes = user.getTomato() != null ? user.getTomato() : 0;
            user.setTomato(currentTomatoes + 1);
            userMapper.updateById(user);
        }

        // 返回更新后的货币信息
        UserCurrency updatedCurrency = userCurrencyMapper.findByUserId(userId);
        CurrencyResponse currencyResponse = convertToCurrencyResponse(updatedCurrency, userId);

        return ApiResponse.<CurrencyResponse>builder()
                .success(true)
                .message("签到成功！获得 1 个番茄 🍅")
                .data(currencyResponse)
                .build();
    }

    /**
     * 补签功能
     * 功能：
     * 1. 检查补签日期是否在允许范围内（过去7天内）
     * 2. 检查该日期是否已签到
     * 3. 检查用户番茄是否足够（需要10个番茄）
     * 4. 扣除10个番茄
     * 5. 创建签到记录
     * 6. 更新本月签到天数
     */
    @Transactional
    public ApiResponse<CurrencyResponse> makeupCheckIn(String token, LocalDate targetDate) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        LocalDate today = LocalDate.now();

        // 检查补签日期不能是今天或未来
        if (targetDate.isAfter(today) || targetDate.isEqual(today)) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message("不能补签今天或未来的日期，今天请使用正常签到功能")
                    .build();
        }

        // 检查补签日期是否在允许范围内（过去7天内）
        long daysBetween = ChronoUnit.DAYS.between(targetDate, today);
        if (daysBetween > 7) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message("只能补签过去7天内的日期")
                    .build();
        }

        // 检查该日期是否已签到
        CheckInRecord existingRecord = checkInRecordMapper.findByUserIdAndDate(userId, targetDate);
        if (existingRecord != null) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message("该日期已签到，无需补签")
                    .build();
        }

        // 检查用户番茄是否足够（需要10个番茄）
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        int currentTomatoes = user.getTomato() != null ? user.getTomato() : 0;
        int requiredTomatoes = 10;
        if (currentTomatoes < requiredTomatoes) {
            return ApiResponse.<CurrencyResponse>builder()
                    .success(false)
                    .message(String.format("番茄不足，补签需要 %d 个番茄，当前只有 %d 个", requiredTomatoes, currentTomatoes))
                    .build();
        }

        // 扣除10个番茄
        user.setTomato(currentTomatoes - requiredTomatoes);
        userMapper.updateById(user);

        // 创建签到记录
        CheckInRecord checkInRecord = new CheckInRecord();
        checkInRecord.setUserId(userId);
        checkInRecord.setCheckinDate(targetDate);
        checkInRecord.setCreatedAt(LocalDateTime.now());
        checkInRecordMapper.insert(checkInRecord);

        // 获取或创建用户货币记录
        UserCurrency currency = userCurrencyMapper.findByUserId(userId);
        if (currency == null) {
            currency = new UserCurrency();
            currency.setUserId(userId);
            currency.setCoins(0);
            currency.setCheckDay(0);
            currency.setUpdatedAt(LocalDateTime.now());
            userCurrencyMapper.insert(currency);
        }

        // 更新本月签到天数
        int targetYear = targetDate.getYear();
        int targetMonth = targetDate.getMonthValue();
        Integer checkInDays = checkInRecordMapper.countCheckInDaysByMonth(userId, targetYear, targetMonth);
        currency.setCheckDay(checkInDays != null ? checkInDays : 1);
        currency.setUpdatedAt(LocalDateTime.now());
        userCurrencyMapper.updateById(currency);

        // 返回更新后的货币信息
        UserCurrency updatedCurrency = userCurrencyMapper.findByUserId(userId);
        CurrencyResponse currencyResponse = convertToCurrencyResponse(updatedCurrency, userId);

        return ApiResponse.<CurrencyResponse>builder()
                .success(true)
                .message(String.format("补签成功！已补签 %s，消耗 %d 个番茄", targetDate, requiredTomatoes))
                .data(currencyResponse)
                .build();
    }

    /**
     * 获取指定用户信息（根据隐私设置过滤）
     */
    public ApiResponse<PublicUserResponse> getUserByUsername(String token, String username) {
        // 查找目标用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return ApiResponse.<PublicUserResponse>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }
        
        // 获取目标用户的隐私设置
        UserPrivacy privacy = userPrivacyMapper.findByUserId(user.getUserId());
        if (privacy == null) {
            return ApiResponse.<PublicUserResponse>builder()
                    .success(false)
                    .message("用户隐私设置不存在")
                    .build();
        }
        
        PublicUserResponse publicUserResponse = convertToPublicUserResponse(user, privacy);
        
        return ApiResponse.<PublicUserResponse>builder()
                .success(true)
                .message("获取成功")
                .data(publicUserResponse)
                .build();
    }

    /**
     * 开始一次专注
     */
    @Transactional
    public ApiResponse<FocusResponse> startFocus(String token, StartFocusRequest req) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<FocusResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<FocusResponse>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }
        
        // 检查用户是否已经在专注中
        if ("专注中".equals(user.getStatus())) {
            return ApiResponse.<FocusResponse>builder()
                    .success(false)
                    .message("您已经在专注中")
                    .build();
        }

        // 查找对应的任务
        Task task = taskMapper.findByUserIdAndTaskName(userId, req.getTask_name());
        if (task == null) {
            return ApiResponse.<FocusResponse>builder()
                    .success(false)
                    .message("任务不存在")
                    .build();
        }
        
        // 检查任务状态
        if ("进行中".equals(task.getStatus())) {
            return ApiResponse.<FocusResponse>builder()
                    .success(false)
                    .message("该任务已经在进行中")
                    .build();
        }

        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = startTime.format(formatter);

        // 更新任务状态为"进行中"并设置开始时间（不更新updated_at）
        // 使用UpdateWrapper只更新status和start_time字段，确保不更新updated_at
        UpdateWrapper<Task> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", task.getId())
                .set("status", "进行中")
                .set("start_time", startTime);
        taskMapper.update(null, updateWrapper);

        // 更新用户状态为"专注中"
        user.setStatus("专注中");
        userMapper.updateById(user);

        // 获取用户的房间ID（如果用户在房间中）
        Long roomId = null;
        RoomMember roomMember = roomMemberMapper.findByUserId(userId);
        if (roomMember != null) {
            roomId = roomMember.getRoomId();
        }

        // 创建专注会话记录
        FocusSession focusSession = new FocusSession();
        focusSession.setUserId(userId);
        // room_id 允许为 NULL（用户可能不在房间中专注）
        focusSession.setRoomId(roomId); // 如果不在房间中，设置为 null
        focusSession.setTaskId(task.getId());
        focusSession.setSessionType("专注学习");
        focusSession.setDuration(task.getDuration() != null ? task.getDuration() : 25); // 默认25分钟
        focusSession.setStartTime(startTime);
        focusSession.setStatus("进行中");
        focusSession.setCreatedAt(startTime);
        focusSessionMapper.insert(focusSession);

        // 构建响应
        FocusResponse focusResponse = FocusResponse.builder()
                .task_name(req.getTask_name())
                .start_time(startTimeStr)
                .build();

        return ApiResponse.<FocusResponse>builder()
                .success(true)
                .message("开始专注成功")
                .data(focusResponse)
                .build();
    }

    /**
     * 结束专注
     */
    @Transactional
    public ApiResponse<StopFocusResponse> stopFocus(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<StopFocusResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<StopFocusResponse>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }
        
        // 检查用户是否在专注中
        if (!"专注中".equals(user.getStatus())) {
            return ApiResponse.<StopFocusResponse>builder()
                    .success(false)
                    .message("您当前没有在专注中")
                    .build();
        }

        // 查找正在进行的任务
        Task task = taskMapper.findFirstByUserIdAndStatus(userId, "进行中");
        if (task == null) {
            // 如果没有找到进行中的任务，只更新用户状态
            user.setStatus("在线");
            userMapper.updateById(user);
            return ApiResponse.<StopFocusResponse>builder()
                    .success(false)
                    .message("未找到正在进行的任务")
                    .build();
        }
        
        // 记录结束时间
        LocalDateTime endTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String endTimeStr = endTime.format(formatter);

        // 计算实际专注时长（分钟）
        Integer actualDuration = 0;
        if (task.getStartTime() != null) {
            long minutes = java.time.Duration.between(task.getStartTime(), endTime).toMinutes();
            actualDuration = (int) minutes;
        }

        // 更新任务状态为"已完成"，设置结束时间和实际专注时长
        task.setStatus("已完成");
        task.setEndTime(endTime);
        task.setActualDuration(actualDuration);
        task.setUpdatedAt(endTime);
        taskMapper.updateById(task);

        // 更新用户状态为"在线"
        user.setStatus("在线");
        userMapper.updateById(user);

        // 更新专注会话记录
        // 查找最新的"进行中"状态的专注会话
        FocusSession focusSession = focusSessionMapper.findLatestByUserIdAndStatus(userId, "进行中");
        if (focusSession != null) {
            // 如果任务ID匹配，或者任务ID为空（可能是旧数据），都更新
            if (focusSession.getTaskId() == null || focusSession.getTaskId().equals(task.getId())) {
                focusSession.setEndTime(endTime);
                focusSession.setStatus("已完成");
                focusSessionMapper.updateById(focusSession);
            } else {
                // 如果任务ID不匹配，仍然更新（可能是任务被切换了）
                focusSession.setEndTime(endTime);
                focusSession.setStatus("已完成");
                focusSessionMapper.updateById(focusSession);
            }
        }

        // 构建响应
        StopFocusResponse stopFocusResponse = StopFocusResponse.builder()
                .task_name(task.getTaskName())
                .end_time(endTimeStr)
                .actual_duration(actualDuration)
                .build();

        return ApiResponse.<StopFocusResponse>builder()
                .success(true)
                .message("结束专注成功")
                .data(stopFocusResponse)
                .build();
    }

    /**
     * 获取专注记录
     */
    public ApiResponse<java.util.List<FocusRecordResponse>> getFocusReport(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<java.util.List<FocusRecordResponse>>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<java.util.List<FocusRecordResponse>>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        // 获取已完成的任务（专注记录）
        java.util.List<Task> completedTasks = taskMapper.findByUserIdAndStatus(userId, "已完成");
        
        // 按创建时间倒序排列（最新的在前）
        completedTasks.sort((t1, t2) -> {
            if (t1.getCreatedAt() == null && t2.getCreatedAt() == null) return 0;
            if (t1.getCreatedAt() == null) return 1;
            if (t2.getCreatedAt() == null) return -1;
            return t2.getCreatedAt().compareTo(t1.getCreatedAt());
        });

        // 转换为响应 DTO
        java.util.List<FocusRecordResponse> records = completedTasks.stream()
                .map(this::convertToFocusRecordResponse)
                .collect(Collectors.toList());

        return ApiResponse.<java.util.List<FocusRecordResponse>>builder()
                .success(true)
                .message("获取专注记录成功")
                .data(records)
                .build();
    }

    /**
     * 获取学习报告
     */
    @Transactional
    public ApiResponse<java.util.List<UserReportResponse>> getUserReports(String token, String reportType) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<java.util.List<UserReportResponse>>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<java.util.List<UserReportResponse>>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        // 验证报告类型
        if (reportType == null || reportType.isEmpty()) {
            return ApiResponse.<java.util.List<UserReportResponse>>builder()
                    .success(false)
                    .message("报告类型不能为空，应为 weekly 或 monthly")
                    .build();
        }

        if (!"weekly".equals(reportType) && !"monthly".equals(reportType)) {
            return ApiResponse.<java.util.List<UserReportResponse>>builder()
                    .success(false)
                    .message("报告类型无效，应为 weekly 或 monthly")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();
        
        // 计算时间范围
        LocalDateTime startTime;
        LocalDateTime endTime = now;
        String reportDate;
        String title;

        java.time.LocalDate reportDateLocal;
        if ("weekly".equals(reportType)) {
            // 近一周
            startTime = now.minus(7, ChronoUnit.DAYS);
            reportDateLocal = now.toLocalDate();
            title = "本周学习报告";
        } else {
            // 近一月
            startTime = now.minus(30, ChronoUnit.DAYS);
            reportDateLocal = now.toLocalDate();
            title = "本月学习报告";
        }

        // 从 tasks 表查询已完成的任务
        List<Task> completedTasks = taskMapper.findCompletedTasksByUserIdAndTimeRange(userId, startTime, endTime);

        // 统计数据
        int totalTasks = completedTasks.size();
        int totalFocusTime = completedTasks.stream()
                .mapToInt(task -> task.getActualDuration() != null ? task.getActualDuration() : 0)
                .sum();
        float avgDailyDuration = 0;
        if ("weekly".equals(reportType) && totalTasks > 0) {
            avgDailyDuration = (float) totalFocusTime / 7.0f;
        } else if ("monthly".equals(reportType) && totalTasks > 0) {
            avgDailyDuration = (float) totalFocusTime / 30.0f;
        }

        // 检查是否已存在相同周期的报告
        List<UserReport> existingReports = userReportMapper.findByUserIdAndReportTypeOrderByCreatedAtDesc(userId, reportType);
        UserReport report;
        
        // 查找是否有相同 report_date 的报告
        UserReport existingReport = existingReports.stream()
                .filter(r -> reportDateLocal.equals(r.getReportDate()))
                .findFirst()
                .orElse(null);

        if (existingReport != null) {
            // 更新现有报告
            report = existingReport;
            report.setTotalFocusTime(totalFocusTime);
            report.setCompletedTasks(totalTasks);
            report.setAvgDailyDuration(avgDailyDuration);
            userReportMapper.updateById(report);
        } else {
            // 创建新报告
            report = new UserReport();
            report.setUserId(userId);
            report.setReportType(reportType);
            report.setReportDate(reportDateLocal);
            report.setTotalFocusTime(totalFocusTime);
            report.setCompletedTasks(totalTasks);
            report.setAvgDailyDuration(avgDailyDuration);
            report.setCreatedAt(now);
            userReportMapper.insert(report);
        }

        // 转换为响应 DTO
        UserReportResponse reportResponse = convertToUserReportResponse(report);
        java.util.List<UserReportResponse> reportResponses = java.util.Collections.singletonList(reportResponse);

        return ApiResponse.<java.util.List<UserReportResponse>>builder()
                .success(true)
                .message("获取学习报告成功")
                .data(reportResponses)
                .build();
    }

    /**
     * 创建待办任务
     */
    @Transactional
    public ApiResponse<TaskResponse> createTask(String token, CreateTaskRequest req) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<TaskResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<TaskResponse>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        LocalDateTime now = LocalDateTime.now();

        // 创建任务
        Task task = new Task();
        task.setTaskId(-1L); // 临时值，保存后会更新为 id 的值
        task.setUserId(userId);
        task.setTaskName(req.getTask_name());
        task.setTaskNote(req.getTask_note() != null ? req.getTask_note() : "");
        task.setDuration(req.getDuration());
        task.setActualDuration(0);
        task.setStatus("未完成");
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        // 保存任务，获取自增的 id
        taskMapper.insert(task);
        
        // 将 task_id 设置为与 id 相同的值
        if (task.getId() != null) {
            task.setTaskId(task.getId());
            taskMapper.updateById(task);
        }

        // 转换为响应 DTO
        TaskResponse taskResponse = convertToTaskResponse(task);

        return ApiResponse.<TaskResponse>builder()
                .success(true)
                .message("新建任务成功")
                .data(taskResponse)
                .build();
    }

    /**
     * 删除待办任务
     */
    @Transactional
    public ApiResponse<Void> deleteTask(String token, DeleteTaskRequest req) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        // 查找任务
        Task task = taskMapper.findByTaskIdAndUserId(req.getTask_id(), userId);
        if (task == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("任务不存在或无权限删除")
                    .build();
        }

        // 删除任务
        taskMapper.deleteById(task.getId());

        return ApiResponse.<Void>builder()
                .success(true)
                .message("删除任务成功")
                .build();
    }

    /**
     * 更新待办任务
     */
    @Transactional
    public java.util.Map<String, Object> updateTask(String token, UpdateTaskRequest req) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "无效的 token");
            return response;
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            response.put("success", false);
            response.put("message", "用户不存在");
            return response;
        }

        if (req.getTask_id() == null) {
            response.put("success", false);
            response.put("message", "任务ID不能为空");
            return response;
        }

        // 根据 task_id 精确查找任务
        Task task = taskMapper.findByTaskIdAndUserId(req.getTask_id(), userId);
        if (task == null) {
            response.put("success", false);
            response.put("message", "任务不存在或无权编辑");
            return response;
        }

        // 更新任务字段（只更新提供的字段）
        boolean hasUpdate = false;
        String newStatus = null;
        if (req.getTask_name() != null && !req.getTask_name().trim().isEmpty()) {
            task.setTaskName(req.getTask_name().trim());
            hasUpdate = true;
        }
        if (req.getTask_note() != null) {
            task.setTaskNote(req.getTask_note());
            hasUpdate = true;
        }
        if (req.getDuration() != null && req.getDuration() > 0) {
            task.setDuration(req.getDuration());
            hasUpdate = true;
        }
        if (req.getStatus() != null && !req.getStatus().trim().isEmpty()) {
            newStatus = req.getStatus().trim();
            task.setStatus(newStatus);
            hasUpdate = true;
        }

        if (!hasUpdate) {
            response.put("success", false);
            response.put("message", "没有需要更新的字段");
            response.put("task_id", task.getTaskId());
            response.put("user_id", task.getUserId());
            response.put("task_name", task.getTaskName());
            response.put("task_note", task.getTaskNote());
            response.put("duration", task.getDuration());
            response.put("status", task.getStatus());
            if (task.getUpdatedAt() != null) {
                response.put("updated_at", task.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                response.put("updated_at", null);
            }
            return response;
        }

        // 更新 updated_at
        LocalDateTime now = LocalDateTime.now();
        task.setUpdatedAt(now);

        // 根据状态变化，自动设置开始时间或结束时间
        // 如果状态变为"进行中"，设置开始时间
        if ("进行中".equals(newStatus) && task.getStartTime() == null) {
            task.setStartTime(now);
        }
        // 如果状态变为"已完成"，设置结束时间
        if ("已完成".equals(newStatus) && task.getEndTime() == null) {
            task.setEndTime(now);
            // 如果开始时间存在，计算实际专注时长
            if (task.getStartTime() != null) {
                long minutes = java.time.Duration.between(task.getStartTime(), now).toMinutes();
                task.setActualDuration((int) minutes);
            }
        }

        // 保存更新
        taskMapper.updateById(task);

        // 构建响应
        response.put("success", true);
        response.put("message", "更新任务成功");
        response.put("task_id", task.getTaskId());
        response.put("user_id", task.getUserId());
        response.put("task_name", task.getTaskName());
        response.put("task_note", task.getTaskNote());
        response.put("duration", task.getDuration());
        response.put("status", task.getStatus());
        response.put("updated_at", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return response;
    }

    /**
     * 发送好友申请
     */
    @Transactional
    public ApiResponse<Void> sendFriendRequest(String token, FriendRequestRequest req) {
        Long fromUserId = getUserIdFromToken(token);
        if (fromUserId == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User fromUser = userMapper.findByUserId(fromUserId);
        if (fromUser == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        String toUserName = req.getUser_name();
        if (toUserName == null || toUserName.trim().isEmpty()) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("用户名不能为空")
                    .build();
        }

        // 根据用户名查找接收用户
        User toUser = userMapper.findByUsername(toUserName.trim());
        if (toUser == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        Long toUserId = toUser.getUserId();

        // 不能给自己发送好友申请
        if (fromUserId.equals(toUserId)) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("不能给自己发送好友申请")
                    .build();
        }

        // 检查是否已经是好友
        if (friendMapper.existsByUserIdAndFriendId(fromUserId, toUserId) || 
            friendMapper.existsByUserIdAndFriendId(toUserId, fromUserId)) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("该用户已经是您的好友")
                    .build();
        }

        // 检查是否已经发送过好友申请（只检查待处理状态）
        FriendRequest existingRequest = friendRequestMapper.findByFromUserIdAndToUserId(fromUserId, toUserId);
        if (existingRequest != null && "待处理".equals(existingRequest.getStatus())) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("已经发送过好友申请，请等待对方处理")
                    .build();
        }
        
        // 如果存在已处理或已拒绝的好友请求记录，删除它以便重新发送
        if (existingRequest != null && !"待处理".equals(existingRequest.getStatus())) {
            friendRequestMapper.deleteById(existingRequest.getId());
        }

        // 创建好友申请
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setFromUserId(fromUserId);
        friendRequest.setFromUserName(fromUser.getUsername());
        friendRequest.setToUserId(toUserId);
        friendRequest.setToUserName(toUser.getUsername());
        friendRequest.setMessage(req.getMessage() != null ? req.getMessage() : "");
        friendRequest.setStatus("待处理");

        friendRequestMapper.insert(friendRequest);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("申请成功")
                .build();
    }

    /**
     * 查看好友申请
     */
    public ApiResponse<java.util.List<FriendRequestResponse>> getFriendRequests(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<java.util.List<FriendRequestResponse>>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<java.util.List<FriendRequestResponse>>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        // 查询发送给当前用户的好友申请（待处理状态）
        List<FriendRequest> requests = friendRequestMapper.findByToUserIdAndStatus(userId, "待处理");

        // 转换为响应 DTO
        java.util.List<FriendRequestResponse> responseList = new java.util.ArrayList<>();
        for (FriendRequest request : requests) {
            // 获取发送者信息
            User fromUser = userMapper.findByUserId(request.getFromUserId());
            String fromUsername = fromUser != null ? fromUser.getUsername() : "未知用户";

            // 获取接收者信息（当前用户）
            String toUsername = user.getUsername();

            FriendRequestResponse response = FriendRequestResponse.builder()
                    .id(request.getId())
                    .from_user_id(request.getFromUserId())
                    .from_username(fromUsername)
                    .to_user_id(request.getToUserId())
                    .to_username(toUsername)
                    .message(request.getMessage())
                    .status(request.getStatus())
                    .build();

            responseList.add(response);
        }

        return ApiResponse.<java.util.List<FriendRequestResponse>>builder()
                .success(true)
                .message("获取好友申请成功")
                .data(responseList)
                .build();
    }

    /**
     * 处理好友申请
     */
    @Transactional
    public ApiResponse<Void> processFriendRequest(String token, ProcessFriendRequestRequest req) {
        Long toUserId = getUserIdFromToken(token);
        if (toUserId == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User toUser = userMapper.findByUserId(toUserId);
        if (toUser == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        Long fromUserId = req.getFrom_user_id();
        if (fromUserId == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("发送申请的用户ID不能为空")
                    .build();
        }

        String action = req.getAction();
        if (action == null || action.trim().isEmpty()) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("操作不能为空，应为 accept 或 reject")
                    .build();
        }

        action = action.trim().toLowerCase();
        if (!"accept".equals(action) && !"reject".equals(action)) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("操作无效，应为 accept 或 reject")
                    .build();
        }

        // 查找好友申请
        FriendRequest friendRequest = friendRequestMapper.findByFromUserIdAndToUserId(fromUserId, toUserId);
        if (friendRequest == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("好友申请不存在")
                    .build();
        }

        // 验证申请状态
        if (!"待处理".equals(friendRequest.getStatus())) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("该好友申请已处理")
                    .build();
        }

        // 验证接收用户是否为当前用户
        if (!toUserId.equals(friendRequest.getToUserId())) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("无权限处理该好友申请")
                    .build();
        }

        // 更新申请状态
        if ("accept".equals(action)) {
            friendRequest.setStatus("已同意");
            
            // 检查发送者是否存在
            User fromUser = userMapper.findByUserId(fromUserId);
            if (fromUser == null) {
                return ApiResponse.<Void>builder()
                        .success(false)
                        .message("发送申请的用户不存在")
                        .build();
            }

            // 检查是否已经是好友
            if (friendMapper.existsByUserIdAndFriendId(toUserId, fromUserId)) {
                friendRequest.setStatus("已同意");
                friendRequestMapper.updateById(friendRequest);
                return ApiResponse.<Void>builder()
                        .success(true)
                        .message("处理成功")
                        .build();
            }

            // 获取发送者的状态
            String fromUserStatus = fromUser.getStatus() != null ? fromUser.getStatus() : "离线";
            
            // 获取接收者的状态
            String toUserStatus = toUser.getStatus() != null ? toUser.getStatus() : "离线";

            // 创建双向好友关系
            
            // 1. 当前用户 -> 发送者
            Friend friend1 = new Friend();
            friend1.setUserId(toUserId);
            friend1.setFriendId(fromUserId);
            friend1.setFriendStatus(fromUserStatus);
            friendMapper.insert(friend1);

            // 2. 发送者 -> 当前用户
            Friend friend2 = new Friend();
            friend2.setUserId(fromUserId);
            friend2.setFriendId(toUserId);
            friend2.setFriendStatus(toUserStatus);
            friendMapper.insert(friend2);
        } else {
            // 拒绝申请
            friendRequest.setStatus("已拒绝");
        }

        friendRequestMapper.updateById(friendRequest);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("处理成功")
                .build();
    }

    /**
     * 获取好友列表
     */
    public ApiResponse<java.util.List<FriendResponse>> getFriends(String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<java.util.List<FriendResponse>>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<java.util.List<FriendResponse>>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        // 查询当前用户的所有好友
        List<Friend> friends = friendMapper.findByUserId(userId);

        // 转换为响应 DTO
        java.util.List<FriendResponse> responseList = new java.util.ArrayList<>();
        for (Friend friend : friends) {
            // 获取好友信息
            User friendUser = userMapper.findByUserId(friend.getFriendId());
            if (friendUser != null) {
                // 始终使用用户当前的 status 字段，而不是 friend 表中缓存的 friend_status
                String currentStatus = friendUser.getStatus() != null ? friendUser.getStatus() : "离线";

                FriendResponse response = FriendResponse.builder()
                        .friend_id(friend.getFriendId())
                        .friend_username(friendUser.getUsername())
                        .friend_status(currentStatus)
                        .build();

                responseList.add(response);
            }
        }

        return ApiResponse.<java.util.List<FriendResponse>>builder()
                .success(true)
                .message("获取好友列表成功")
                .data(responseList)
                .build();
    }

    /**
     * 删除好友
     */
    @Transactional
    public ApiResponse<Void> deleteFriend(String token, DeleteFriendRequest req) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        User user = userMapper.findByUserId(userId);
        if (user == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        String friendUsername = req.getFriend_name();
        if (friendUsername == null || friendUsername.trim().isEmpty()) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("好友用户名不能为空")
                    .build();
        }

        // 根据用户名查找好友
        User friendUser = userMapper.findByUsername(friendUsername.trim());
        if (friendUser == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("好友不存在")
                    .build();
        }

        Long friendId = friendUser.getUserId();

        // 检查是否是好友关系
        Friend friend1 = friendMapper.findByUserIdAndFriendId(userId, friendId);
        Friend friend2 = friendMapper.findByUserIdAndFriendId(friendId, userId);

        if (friend1 == null && friend2 == null) {
            return ApiResponse.<Void>builder()
                    .success(false)
                    .message("该用户不是你的好友")
                    .build();
        }

        // 删除双向好友关系
        if (friend1 != null) {
            friendMapper.deleteById(friend1.getId());
        }
        if (friend2 != null) {
            friendMapper.deleteById(friend2.getId());
        }

        // 清理相关的好友请求记录，允许重新添加
        // 删除两个方向的好友请求记录（无论状态如何）
        FriendRequest request1 = friendRequestMapper.findByFromUserIdAndToUserId(userId, friendId);
        if (request1 != null) {
            friendRequestMapper.deleteById(request1.getId());
        }
        FriendRequest request2 = friendRequestMapper.findByFromUserIdAndToUserId(friendId, userId);
        if (request2 != null) {
            friendRequestMapper.deleteById(request2.getId());
        }

        return ApiResponse.<Void>builder()
                .success(true)
                .message("删除好友成功")
                .build();
    }

    /**
     * 获取好友统计数据
     * @param token 当前用户的token
     * @param friendUsername 好友用户名
     */
    public ApiResponse<FriendStatsResponse> getFriendStats(String token, String friendUsername) {
        Long currentUserId = getUserIdFromToken(token);
        if (currentUserId == null) {
            return ApiResponse.<FriendStatsResponse>builder()
                    .success(false)
                    .message("无效的 token")
                    .build();
        }

        // 查找好友用户
        User friendUser = userMapper.findByUsername(friendUsername);
        if (friendUser == null) {
            return ApiResponse.<FriendStatsResponse>builder()
                    .success(false)
                    .message("用户不存在")
                    .build();
        }

        Long friendUserId = friendUser.getUserId();

        // 验证是否是好友关系
        boolean isFriend = friendMapper.existsByUserIdAndFriendId(currentUserId, friendUserId) ||
                          friendMapper.existsByUserIdAndFriendId(friendUserId, currentUserId);
        if (!isFriend) {
            return ApiResponse.<FriendStatsResponse>builder()
                    .success(false)
                    .message("该用户不是您的好友")
                    .build();
        }

        // 检查隐私设置
        UserPrivacy privacy = userPrivacyMapper.findByUserId(friendUserId);
        if (privacy != null && "private".equals(privacy.getShowStudyTime())) {
            return ApiResponse.<FriendStatsResponse>builder()
                    .success(false)
                    .message("该用户已设置隐私，不允许查看学习时间")
                    .build();
        }

        // 计算统计数据
        LocalDate today = LocalDate.now();

        // 1. 今日番茄数（今日完成的任务数）
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        List<Task> todayCompletedTasks = taskMapper.findCompletedTasksByUserIdAndTimeRange(
                friendUserId, todayStart, todayEnd);
        int todayTomatoes = todayCompletedTasks.size();

        // 2. 本周学习时长（按天统计）
        List<FriendStatsResponse.WeeklyHourData> weeklyHours = new java.util.ArrayList<>();
        String[] dayNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            
            List<Task> dayTasks = taskMapper.findCompletedTasksByUserIdAndTimeRange(
                    friendUserId, dayStart, dayEnd);
            int totalMinutes = dayTasks.stream()
                    .mapToInt(task -> task.getActualDuration() != null ? task.getActualDuration() : 0)
                    .sum();
            int hours = totalMinutes / 60; // 转换为小时
            
            weeklyHours.add(FriendStatsResponse.WeeklyHourData.builder()
                    .day(dayNames[6 - i])
                    .hours(hours)
                    .build());
        }

        // 3. 本月任务完成数（按日期统计，每5天一个点）
        List<FriendStatsResponse.MonthlyTaskData> monthlyTasks = new java.util.ArrayList<>();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();
        LocalDate monthEnd = today;
        
        // 统计每5天的任务完成数
        for (int day = 1; day <= monthEnd.getDayOfMonth(); day += 5) {
            LocalDate startDate = LocalDate.of(currentYear, currentMonth, day);
            LocalDate endDate = startDate.plusDays(4);
            if (endDate.isAfter(monthEnd)) {
                endDate = monthEnd;
            }
            
            LocalDateTime rangeStart = startDate.atStartOfDay();
            LocalDateTime rangeEnd = endDate.plusDays(1).atStartOfDay();
            
            List<Task> rangeTasks = taskMapper.findCompletedTasksByUserIdAndTimeRange(
                    friendUserId, rangeStart, rangeEnd);
            
            monthlyTasks.add(FriendStatsResponse.MonthlyTaskData.builder()
                    .date(String.valueOf(day))
                    .count(rangeTasks.size())
                    .build());
        }

        // 4. 最常学习的科目（按任务名称统计，取前3个）
        List<Task> allCompletedTasks = taskMapper.findByUserIdAndStatus(friendUserId, "已完成");
        Map<String, Integer> subjectCount = new java.util.HashMap<>();
        for (Task task : allCompletedTasks) {
            String taskName = task.getTaskName();
            if (taskName != null && !taskName.trim().isEmpty()) {
                subjectCount.put(taskName, subjectCount.getOrDefault(taskName, 0) + 1);
            }
        }
        
        List<FriendStatsResponse.TopSubjectData> topSubjects = subjectCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .map(entry -> FriendStatsResponse.TopSubjectData.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // 构建响应
        FriendStatsResponse stats = FriendStatsResponse.builder()
                .todayTomatoes(todayTomatoes)
                .weeklyHours(weeklyHours)
                .monthlyTasks(monthlyTasks)
                .topSubjects(topSubjects)
                .build();

        return ApiResponse.<FriendStatsResponse>builder()
                .success(true)
                .message("获取好友统计数据成功")
                .data(stats)
                .build();
    }

    /**
     * 将 User 实体转换为 UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        Long birthdayTimestamp = null;
        if (user.getBirthday() != null) {
            birthdayTimestamp = user.getBirthday()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        }

        return UserResponse.builder()
                .user_id(user.getUserId())
                .username(user.getUsername())
                .status(user.getStatus())
                .email(user.getEmail())
                .phone(user.getPhone())
                .sex(user.getSex())
                .birthday(birthdayTimestamp)
                .tomato(user.getTomato())
                .province(user.getProvince())
                .avatar(user.getAvatar()) // 包含头像URL
                .build();
    }

    /**
     * 将 UserPrivacy 实体转换为 PrivacyResponse DTO
     */
    private PrivacyResponse convertToPrivacyResponse(UserPrivacy privacy) {
        return PrivacyResponse.builder()
                .show_birthday(privacy.getShowBirthday() != null ? privacy.getShowBirthday() : "public")
                .show_study_time(privacy.getShowStudyTime() != null ? privacy.getShowStudyTime() : "public")
                .show_location(privacy.getShowLocation() != null ? privacy.getShowLocation() : "public")
                .allow_friend_request(privacy.getAllowFriendRequest() != null && privacy.getAllowFriendRequest() == 1)
                .searchable(privacy.getSearchable() != null && privacy.getSearchable() == 1)
                .build();
    }

    /**
     * 将 UserCurrency 实体转换为 CurrencyResponse DTO（不带签到状态，用于兼容旧代码）
     */
    private CurrencyResponse convertToCurrencyResponse(UserCurrency currency) {
        return convertToCurrencyResponse(currency, null);
    }

    /**
     * 将 UserCurrency 实体转换为 CurrencyResponse DTO（带签到状态）
     */
    private CurrencyResponse convertToCurrencyResponse(UserCurrency currency, Long userId) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        String updatedAtStr = null;
        if (currency.getUpdatedAt() != null) {
            updatedAtStr = currency.getUpdatedAt().format(dateFormatter);
        }
        
        // 检查今天是否已签到
        Boolean hasCheckedInToday = false;
        if (userId != null) {
            LocalDate today = LocalDate.now();
            CheckInRecord todayRecord = checkInRecordMapper.findByUserIdAndDate(userId, today);
            hasCheckedInToday = (todayRecord != null);
        }
        
        return CurrencyResponse.builder()
                .user_id(currency.getUserId())
                .coins(currency.getCoins())
                .check_day(currency.getCheckDay())
                .updated_at(updatedAtStr)
                .has_checked_in_today(hasCheckedInToday)
                .build();
    }

    /**
     * 将 User 实体转换为 PublicUserResponse DTO（根据隐私设置过滤）
     */
    private PublicUserResponse convertToPublicUserResponse(User user, UserPrivacy privacy) {
        Long birthdayTimestamp = null;
        // 如果生日设置为public或friends，则显示
        if (user.getBirthday() != null && privacy.getShowBirthday() != null 
                && !"private".equals(privacy.getShowBirthday())) {
            birthdayTimestamp = user.getBirthday()
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        }

        // 根据隐私设置决定是否显示位置
        String province = null;
        if (privacy.getShowLocation() != null && !"private".equals(privacy.getShowLocation())) {
            province = user.getProvince();
        }

        return PublicUserResponse.builder()
                .user_id(user.getUserId())
                .username(user.getUsername())
                .status(user.getStatus())
                .sex(user.getSex())
                .birthday(birthdayTimestamp)
                .tomato(user.getTomato())
                .province(province)
                .build();
    }

    /**
     * 将 Task 实体转换为 TaskResponse DTO
     */
    private TaskResponse convertToTaskResponse(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        String createdAtStr = null;
        if (task.getCreatedAt() != null) {
            createdAtStr = task.getCreatedAt().format(formatter);
        }
        
        return TaskResponse.builder()
                .task_id(task.getTaskId())
                .user_id(task.getUserId())
                .task_name(task.getTaskName())
                .task_note(task.getTaskNote())
                .duration(task.getDuration())
                .status(task.getStatus())
                .created_at(createdAtStr)
                .build();
    }

    /**
     * 将 Task 实体转换为 FocusRecordResponse DTO
     */
    private FocusRecordResponse convertToFocusRecordResponse(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        String startTimeStr = null;
        if (task.getStartTime() != null) {
            startTimeStr = task.getStartTime().format(formatter);
        }
        
        String endTimeStr = null;
        if (task.getEndTime() != null) {
            endTimeStr = task.getEndTime().format(formatter);
        }
        
        String createdAtStr = null;
        if (task.getCreatedAt() != null) {
            createdAtStr = task.getCreatedAt().format(formatter);
        }
        
        return FocusRecordResponse.builder()
                .task_id(task.getTaskId())
                .task_name(task.getTaskName())
                .task_note(task.getTaskNote())
                .duration(task.getDuration())
                .actual_duration(task.getActualDuration())
                .status(task.getStatus())
                .start_time(startTimeStr)
                .end_time(endTimeStr)
                .created_at(createdAtStr)
                .build();
    }

    /**
     * 将 UserReport 实体转换为 UserReportResponse DTO
     */
    private UserReportResponse convertToUserReportResponse(UserReport report) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        String createdAtStr = null;
        if (report.getCreatedAt() != null) {
            createdAtStr = report.getCreatedAt().format(formatter);
        }
        
        // 构建 summary JSON 对象
        Map<String, Object> summaryMap = new HashMap<>();
        summaryMap.put("total_focus_time", report.getTotalFocusTime());
        summaryMap.put("completed_tasks", report.getCompletedTasks());
        summaryMap.put("avg_daily_duration", report.getAvgDailyDuration());
        
        // 生成标题
        String title = "weekly".equals(report.getReportType()) ? "本周学习报告" : "本月学习报告";
        
        // 格式化报告日期
        String reportDateStr = report.getReportDate() != null ? 
            report.getReportDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
        
        return UserReportResponse.builder()
                .id(report.getReportId())
                .user_id(report.getUserId())
                .report_type(report.getReportType())
                .report_date(reportDateStr)
                .title(title)
                .summary(summaryMap)
                .share_image_url("")
                .is_read(0)
                .created_at(createdAtStr)
                .build();
    }
}

