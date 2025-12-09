# 番茄自习室后端整合项目

## 项目概述

本项目整合了两个后端框架：
- `tomato/` - 包含用户认证、好友系统、任务管理、专注记录等功能
- `TomatoStudy-main/backend/` - 包含自习室管理、背景音乐、系统配置等功能

## 技术栈

- Spring Boot 3.3.3
- MyBatis Plus 3.5.4
- Spring Security + JWT
- MySQL 8.0+
- Lombok

## 项目结构

```
backend/
├── src/main/java/com/tomato/
│   ├── TomatoApplication.java          # 主应用类
│   ├── common/                         # 公共类
│   │   ├── ApiResponse.java           # 统一响应格式
│   │   └── GlobalExceptionHandler.java # 全局异常处理
│   ├── config/                         # 配置类
│   │   ├── SecurityConfig.java        # Spring Security 配置
│   │   ├── CorsConfig.java            # 跨域配置
│   │   ├── MyBatisPlusConfig.java     # MyBatis Plus 配置
│   │   └── WebConfig.java             # Web 配置
│   ├── security/                      # 安全相关
│   │   ├── JwtUtil.java               # JWT 工具类
│   │   ├── JwtFilter.java             # JWT 过滤器
│   │   └── TokenBlacklistService.java # Token 黑名单服务
│   ├── entity/                        # 实体类（已整合）
│   ├── mapper/                        # MyBatis Mapper 接口（待完善）
│   ├── service/                       # 服务层（待完善）
│   ├── controller/                    # 控制器层（待完善）
│   └── dto/                           # 数据传输对象（待完善）
└── src/main/resources/
    ├── application.yml                # 配置文件
    └── mapper/                        # MyBatis XML 映射文件
```

## 已完成的工作

✅ 整合 pom.xml 依赖
✅ 统一包名为 `com.tomato`
✅ 整合所有实体类（统一使用 MyBatis Plus 注解）
✅ 整合配置类（Security、CORS、MyBatis Plus、WebConfig）
✅ 整合 JWT 安全认证
✅ 整合异常处理
✅ 创建主应用类

## 待完成的工作

### 1. Mapper 层
需要将 `tomato` 项目中的 JPA Repository 转换为 MyBatis Mapper：
- UserMapper
- TaskMapper
- FriendMapper
- FriendRequestMapper
- UserCurrencyMapper
- UserPrivacyMapper
- UserReportMapper
- RoomMapper（已存在）
- RoomMemberMapper（已存在）
- BackgroundMusicMapper（已存在）
- SystemConfigMapper（已存在）

### 2. Service 层
需要将服务层从 JPA 转换为 MyBatis Plus：
- AuthService（需要修改为使用 Mapper）
- UserService（需要修改为使用 Mapper）
- RoomService（已存在，需要整合）
- TaskService（已存在，需要整合）
- BackgroundMusicService（已存在）
- SystemConfigService（已存在）

### 3. Controller 层
需要整合所有控制器：
- AuthController（已存在，需要修改包名和引用）
- UserController（已存在，需要修改包名和引用）
- RoomController（已存在，需要修改包名和引用）
- TaskController（已存在，需要修改包名和引用）
- BackgroundMusicController（已存在，需要修改包名和引用）
- SystemConfigController（已存在，需要修改包名和引用）

### 4. DTO 层
需要将所有 DTO 类复制并修改包名：
- 从 `org.example.tomato.dto` 复制到 `com.tomato.dto`
- 从 `com.tomato.studyroom.dto` 复制到 `com.tomato.dto`

## 数据库配置

修改 `src/main/resources/application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tomato?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

## 运行项目

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

## 注意事项

1. 所有实体类的 ID 类型已统一为 `Long`
2. 需要将 JPA Repository 的方法转换为 MyBatis Plus 的 Mapper 方法
3. 注意两个项目中 Task 实体的字段可能略有不同，需要统一
4. 数据库表结构需要确保兼容两个项目的需求

## 整合步骤建议

1. 先完成 Mapper 层的转换
2. 然后修改 Service 层使用 Mapper
3. 最后整合 Controller 层
4. 测试所有接口功能

