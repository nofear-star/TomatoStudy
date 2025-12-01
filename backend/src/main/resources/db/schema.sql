-- 番茄自习室数据库表结构
-- 创建数据库
CREATE DATABASE IF NOT EXISTS tomato_study_room DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tomato_study_room;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    user_id BIGINT NOT NULL COMMENT '业务用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    status VARCHAR(10) DEFAULT '在线' COMMENT '在线状态',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    sex VARCHAR(10) COMMENT '性别',
    birthday DATE COMMENT '生日',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
    tomato INT NOT NULL DEFAULT 0 COMMENT '番茄数',
    province VARCHAR(50) COMMENT '省份',
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- 背景音乐表
CREATE TABLE IF NOT EXISTS backgroundmusic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键 音乐编号',
    music_name VARCHAR(100) NOT NULL COMMENT '音乐名称',
    audio_url VARCHAR(500) NOT NULL COMMENT '音频文件地址',
    price decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    is_free tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否免费（0-否，1-是）',
    duration INT COMMENT '时长(秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_music_name (music_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='背景音乐表';

-- 自习室表
CREATE TABLE IF NOT EXISTS room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    room_id BIGINT NOT NULL COMMENT '房间业务ID',
    room_name VARCHAR(100) NOT NULL COMMENT '房间名称',
    create_person BIGINT NOT NULL COMMENT '房主ID(外键关联: user.user_id)',
    max_members INT NOT NULL DEFAULT 20 COMMENT '房间最大人数',
    end_time BIGINT COMMENT '房间结束时间(毫秒)',
    music_id BIGINT COMMENT '背景音乐ID(关联 backgroundmusic.id)',
    music_name VARCHAR(100) COMMENT '房间背景音名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_room_id (room_id),
    INDEX idx_host (create_person),
    INDEX idx_music (music_id),
    CONSTRAINT fk_room_host FOREIGN KEY (create_person) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_room_music FOREIGN KEY (music_id) REFERENCES backgroundmusic(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自习室表';


-- 房间成员表
CREATE TABLE IF NOT EXISTS roommember (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    room_id BIGINT NOT NULL COMMENT '房间业务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(关联 user.user_id)',
    role VARCHAR(10) NOT NULL DEFAULT '成员' COMMENT '角色: 房主/成员',
    status VARCHAR(20) NOT NULL DEFAULT '专注中' COMMENT '状态: 专注中/休息中',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    session_focus_duration INT NOT NULL DEFAULT 0 COMMENT '累计专注时长(分钟)',
    UNIQUE KEY uk_room_user (room_id, user_id),
    INDEX idx_room (room_id),
    INDEX idx_user (user_id),
    CONSTRAINT fk_room_member_room FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE,
    CONSTRAINT fk_room_member_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间成员表';

-- 任务表
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    task_id BIGINT NOT NULL COMMENT '任务业务ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(关联 user.user_id)',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    task_note TEXT COMMENT '任务备注',
    duration INT COMMENT '计划时长(分钟)',
    actual_duration INT DEFAULT 0 COMMENT '实际专注时长(分钟)',
    status VARCHAR(20) DEFAULT '未完成' COMMENT '任务状态: 未完成, 进行中, 已完成',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_task_id (task_id),
    INDEX idx_user_status (user_id, status),
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 专注会话表
CREATE TABLE IF NOT EXISTS focussession (
    session_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    room_id BIGINT 	NOT NULL COMMENT '房间ID',
    session_type VARCHAR(50) NOT NULL COMMENT '会话类型: 专注学习, 短休息, 长休息',
    duration INT NOT NULL COMMENT '时长(分钟)',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status VARCHAR(20) DEFAULT '进行中' COMMENT '状态: 进行中, 已完成, 已取消',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_room_id (room_id),
    INDEX idx_start_time (start_time),
    CONSTRAINT `focussession_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `focussession_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `room` (`room_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `focussession_ibfk_3` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`task_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专注会话表';


-- 系统配置表
CREATE TABLE IF NOT EXISTS system_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键名,唯一',
    config_value TEXT COMMENT '配置值(JSON格式)',
    description VARCHAR(255) COMMENT '配置描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 学习报告表
CREATE TABLE IF NOT EXISTS studyreport (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    report_date DATE NOT NULL COMMENT '报告日期',
    total_study_time INT DEFAULT 0 COMMENT '总学习时长(分钟)',
    completed_sessions INT DEFAULT 0 COMMENT '完成会话数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_date (user_id, report_date),
    INDEX idx_user_id (user_id),
    INDEX idx_report_date (report_date),
    CONSTRAINT `studyreport_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习报告表';

-- 好友表
CREATE TABLE IF NOT EXISTS friend (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    status VARCHAR(20) DEFAULT 'accepted' COMMENT '状态: pending, accepted, blocked',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_friend (user_id, friend_id),
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id),
    CONSTRAINT `friend_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `friend_ibfk_2` FOREIGN KEY (`friend_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友表';

-- 好友请求表
CREATE TABLE IF NOT EXISTS friendrequest (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    receiver_id BIGINT NOT NULL COMMENT '接收者ID',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending, accepted, rejected',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sender_id (sender_id),
    INDEX idx_receiver_id (receiver_id),
    CONSTRAINT `friendrequest_ibfk_1` FOREIGN KEY (`send_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `friendrequest_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友请求表';

-- 用户货币表
CREATE TABLE IF NOT EXISTS usercurrency (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    coins INT DEFAULT 0 COMMENT '金币数量',
    points INT DEFAULT 0 COMMENT '积分数量',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户货币表';

-- 用户隐私设置表
CREATE TABLE IF NOT EXISTS userprivacy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    show_profile VARCHAR(20) DEFAULT 'public' COMMENT '显示资料: public, friends, private',
    show_study_time VARCHAR(20) DEFAULT 'public' COMMENT '显示学习时长: public, friends, private',
    allow_friend_request VARCHAR(20) DEFAULT 'everyone' COMMENT '允许好友请求: everyone, friends_of_friends, none',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    CONSTRAINT `userprivacy_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户隐私设置表';
