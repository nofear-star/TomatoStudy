-- 初始化测试数据
USE tomato_study_room;

-- 插入测试用户
INSERT INTO user (username, password_hash, email) VALUES
('admin', 'admin123', 'admin@example.com''),
('testuser', 'test123', 'test@example.com');

-- 插入背景音乐数据
INSERT INTO backgroundmusic (music_name, audio_url, duration) VALUES
('宁静雨声', 'https://example.com/music/rain.mp3', 1800),
('森林鸟鸣', 'https://example.com/music/forest.mp3', 1500),
('海浪声', 'https://example.com/music/ocean.mp3', 2000),
('白噪音', 'https://example.com/music/whitenoise.mp3', 3600),
('咖啡厅', 'https://example.com/music/cafe.mp3', 2400);

-- 插入系统配置
INSERT INTO system_configs (config_key, config_value, description) VALUES
('focus_time', '25', '默认专注时长(分钟)'),
('short_break', '5', '短休息时长(分钟)'),
('long_break', '15', '长休息时长(分钟)'),
('long_break_interval', '4', '长休息间隔(会话数)'),
('max_room_capacity', '20', '房间最大容量'),
('site_name', '番茄自习室', '网站名称');
