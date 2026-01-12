# 番茄自习室后端项目

## 📋 项目简介
番茄自习室是一个基于番茄工作法的在线自习平台，提供专注学习、任务管理、社交互动等功能。后端采用 Spring Boot 框架构建，提供完整的 RESTful API 接口服务。

## ✨ 功能特性

### 🎯 核心功能
- **用户系统**: 注册、登录、个人资料管理
- **自习室管理**: 创建、加入、管理自习室
- **任务管理**: 番茄任务创建、追踪、统计
- **专注记录**: 专注时长统计、学习报告
- **社交功能**: 好友系统、好友请求、学习监督
- **资源管理**: 背景音乐、系统配置

### 🛡️ 安全特性
- JWT 令牌认证
- 权限控制
- 数据加密传输
- 输入验证

## 🏗️ 系统架构

### 技术栈
- **后端框架**: Spring Boot 3.1.5
- **ORM框架**: MyBatis Plus 3.5.4
- **数据库**: MySQL 8.0+
- **缓存**: Redis (待集成)
- **消息队列**: WebSocket (待集成)
- **构建工具**: Maven 3.6+
- **JDK版本**: 17+

### 架构分层
```
controller/     # 控制器层 - 处理HTTP请求
service/        # 服务层 - 业务逻辑处理
mapper/         # 数据访问层 - MyBatis Plus
entity/         # 实体层 - 数据库映射对象
dto/            # 数据传输对象 - API接口参数/响应
common/         # 公共模块 - 工具类、常量、异常
config/         # 配置模块 - 系统配置
security/       # 安全模块 - 认证授权
```
## 📁 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/tomato/
│   │   │   ├── TomatoApplication.java          # 主应用类
│   │   │   ├── common/                         # 公共类
│   │   │   │   ├── ApiResponse.java            # 统一响应格式
│   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   │   ├── config/                         # 配置类
│   │   │   │   ├── SecurityConfig.java         # Spring Security 配置
│   │   │   │   ├── CorsConfig.java             # 跨域配置
│   │   │   │   ├── MyBatisPlusConfig.java      # MyBatis Plus 配置
│   │   │   │   └── WebConfig.java              # Web 配置
│   │   │   ├── security/                       # 安全相关
│   │   │   │   ├── JwtUtil.java                # JWT 工具类
│   │   │   │   ├── JwtFilter.java              # JWT 过滤器
│   │   │   │   └── TokenBlacklistService.java  # Token 黑名单服务
│   │   │   ├── entity/                          # 实体类
│   │   │   ├── mapper/                          # MyBatis Mapper 接口
│   │   │   ├── service/                         # 服务层
│   │   │   ├── controller/                      # 控制器层
│   │   │   └── dto/                             # 数据传输对象
│   │   └── resources/
│   │       ├── application.yml                  # 配置文件
│   │       └── mapper/                          # MyBatis XML 映射文件
│   └── test/                                     # 测试代码
├── pom.xml                                       # Maven 配置文件
├── README.md                                     # 项目说明文档
└── API_DOCUMENTATION.md                          # API 接口文档
```
## 🗄️ 数据库设计

### 核心数据表（12张）
- `user` - 用户信息表
- `room` - 自习室表
- `roommember` - 房间成员表
- `tasks` - 任务表
- `focussession` - 专注会话表
- `backgroundmusic` - 背景音乐表
- `system_configs` - 系统配置表
- `studyreport` - 学习报告表
- `friend` - 好友关系表
- `friendrequest` - 好友请求表
- `usercurrency` - 用户货币表
- `userprivacy` - 用户隐私设置表

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+ (可选)

### 1. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE tomato
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE tomato;

-- 执行初始化脚本
-- 在MySQL中执行
source backend/src/main/resources/db/schema.sql
source backend/src/main/resources/db/init-data.sql
```

### 2. 配置文件修改

编辑 `src/main/resources/application.yml`:

```yaml
# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tomato?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

# 服务器配置
server:
  port: 8090
  servlet:
    context-path: /api

# JWT配置
jwt:
  secret: your-jwt-secret-key-here-change-in-production
  expiration: 86400000  # 24小时

# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

### 3. 项目编译与运行

**方式一：使用Maven**

```bash
# 进入项目目录
cd backend

# 清理并编译
mvn clean compile

# 运行项目
mvn spring-boot:run
```

**方式二：使用IDE**

1. 导入项目到 IDEA/Eclipse
2. 找到 `TomatoApplication.java`
3. 直接运行主类

## 📝 API 文档

详细的 API 接口文档请参考：[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目采用 MIT 许可证。