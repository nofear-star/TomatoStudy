package com.tomato.service;

import com.tomato.common.ApiResponse;
import com.tomato.dto.*;
import com.tomato.entity.*;
import com.tomato.mapper.*;
import com.tomato.security.JwtUtil;
import com.tomato.security.TokenBlacklistService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final UserCurrencyMapper userCurrencyMapper;
    private final UserPrivacyMapper userPrivacyMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(UserMapper userMapper,
                       UserCurrencyMapper userCurrencyMapper,
                       UserPrivacyMapper userPrivacyMapper,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       TokenBlacklistService tokenBlacklistService) {
        this.userMapper = userMapper;
        this.userCurrencyMapper = userCurrencyMapper;
        this.userPrivacyMapper = userPrivacyMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Transactional
    public ApiResponse<AuthResponse> register(RegisterRequest req) {
        // 唯一性校验
        if (userMapper.existsByUsername(req.getUsername())) {
            return ApiResponse.<AuthResponse>builder().success(false).message("用户名已存在").build();
        }
        if (userMapper.existsByEmail(req.getEmail())) {
            return ApiResponse.<AuthResponse>builder().success(false).message("邮箱已被注册").build();
        }
        // 只有当手机号不为空时才检查唯一性
        if (req.getPhone() != null && !req.getPhone().trim().isEmpty() && userMapper.existsByPhone(req.getPhone())) {
            return ApiResponse.<AuthResponse>builder().success(false).message("手机号已被注册").build();
        }

        // 在插入前再次检查唯一性（防止并发问题）
        // 使用 findByUsername 而不是 existsByUsername，可以获取更详细的信息
        User existingUser = userMapper.findByUsername(req.getUsername());
        if (existingUser != null) {
            return ApiResponse.<AuthResponse>builder().success(false).message("用户名已存在").build();
        }
        User existingEmail = userMapper.findByEmail(req.getEmail());
        if (existingEmail != null) {
            return ApiResponse.<AuthResponse>builder().success(false).message("邮箱已被注册").build();
        }

        // 生成业务 userId
        Long businessUserId = generateBusinessUserId();

        User user = new User();
        user.setUserId(businessUserId);
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        // 如果手机号为空或空字符串，设置为 null（数据库允许 NULL，且 NULL 值不参与唯一性约束）
        String phone = req.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone.trim());
        } else {
            // 设置为 null，数据库允许 NULL 值，且 NULL 值不参与唯一性约束
            // 这样多个用户都可以没有手机号，不会产生唯一性约束冲突
            user.setPhone(null);
        }
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setStatus("离线"); // 注册时默认为离线状态
        user.setTomato(0);
        user.setDeleted(0); // 新注册的用户未删除

        userMapper.insert(user);

        // 创建 usercurrency 默认记录
        UserCurrency uc = new UserCurrency();
        uc.setUserId(user.getUserId());
        uc.setCoins(0);
        uc.setCheckDay(0);
        uc.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()).toLocalDateTime());
        userCurrencyMapper.insert(uc);

        // 创建 userprivacy 默认记录
        UserPrivacy up = new UserPrivacy();
        up.setUserId(user.getUserId());
        up.setShowBirthday("public");
        up.setShowStudyTime("public");
        up.setShowLocation("public");
        up.setAllowFriendRequest(1);
        up.setSearchable(1);
        up.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()).toLocalDateTime());
        userPrivacyMapper.insert(up);

        // 生成 token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        AuthResponse resp = AuthResponse.builder().token(token).userId(user.getUserId()).username(user.getUsername()).build();

        return ApiResponse.<AuthResponse>builder().success(true).message("注册成功").data(resp).build();
    }

    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest req) {
        // 允许用户名/邮箱/手机号任意一种登录
        User user = userMapper.findByUsername(req.getUsername());
        if (user == null) {
            user = userMapper.findByEmail(req.getUsername());
        }
        if (user == null) {
            // 查手机号 - 需要自定义查询，只查询未删除的用户
            List<User> allUsers = userMapper.selectList(null);
            user = allUsers.stream()
                    .filter(u -> (u.getDeleted() == null || u.getDeleted() == 0) 
                            && u.getPhone() != null 
                            && u.getPhone().equals(req.getUsername()))
                    .findFirst()
                    .orElse(null);
        }

        if (user == null) {
            return ApiResponse.<AuthResponse>builder().success(false).message("用户不存在").build();
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ApiResponse.<AuthResponse>builder().success(false).message("密码错误").build();
        }

        // 更新用户状态
        user.setStatus("在线");
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        AuthResponse authResp = AuthResponse.builder().token(token).userId(user.getUserId()).username(user.getUsername()).build();
        return ApiResponse.<AuthResponse>builder().success(true).message("登录成功").data(authResp).build();
    }

    public ApiResponse<Void> logout(String token) {
        if (token == null || token.isBlank()) {
            return ApiResponse.<Void>builder().success(false).message("无效的 token").build();
        }
        if (!jwtUtil.validateToken(token)) {
            return ApiResponse.<Void>builder().success(false).message("token 无效或过期").build();
        }

        Date expiration = jwtUtil.getExpiration(token);
        tokenBlacklistService.blacklist(token, expiration.getTime());

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userMapper.findByUserId(userId);
        if (user != null) {
            user.setStatus("离线");
            userMapper.updateById(user);
        }

        return ApiResponse.<Void>builder().success(true).message("登出成功").build();
    }

    private Long generateBusinessUserId() {
        // 简单生成策略：最大 userId + 1
        List<User> allUsers = userMapper.selectList(null);
        Long max = allUsers.stream()
                .map(User::getUserId)
                .max(Long::compareTo)
                .orElse(1000L);
        return max + 1;
    }
}

