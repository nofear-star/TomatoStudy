# 番茄自习室后端服务

基于SpringBoot的番茄自习室后端API服务。

## 🛠️ 技术栈

- **框架**: Spring Boot 3.1.5
- **ORM**: MyBatis Plus 3.5.4
- **数据库**: MySQL 8.0+
- **JDK**: 17+

## 📁 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/tomato/studyroom/
│   │   │   ├── common/          # 公共类(统一响应、异常处理)
│   │   │   ├── config/          # 配置类(跨域、MyBatis Plus)
│   │   │   ├── controller/      # 控制器层
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── mapper/          # Mapper接口
│   │   │   └── service/         # 服务层
│   │   └── resources/
│   │       ├── mapper/          # MyBatis XML映射文件
│   │       ├── db/              # 数据库脚本
│   │       └── application.yml  # 配置文件
│   └── test/
└── pom.xml                      # Maven配置
```

## 🚀 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE tomato DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行SQL脚本初始化表结构：
```bash
# 在MySQL中执行
source backend/src/main/resources/db/schema.sql
source backend/src/main/resources/db/init-data.sql
```

或者直接导入SQL文件到数据库。

### 3. 配置数据库连接

修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tomato?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 4. 编译和运行

```bash
# 进入backend目录
cd backend

# 使用Maven编译
mvn clean compile

# 运行项目
mvn spring-boot:run
```

或者使用IDE直接运行 `StudyRoomApplication.java` 主类。

### 5. 访问API

服务启动后，默认运行在 `http://localhost:8080/api`

## 📚 API接口文档

### 自习室相关接口

#### 1. 获取自习室列表
```
GET /api/rooms
```

#### 2. 创建自习室
```
POST /api/rooms
Content-Type: application/json

{
  "roomId": "room001",
  "roomName": "学习室1",
  "createPerson": 1,
  "maxMembers": 20,
  "endTime": null,
  "musicName": "宁静雨声"
}
```

#### 3. 获取单个自习室
```
GET /api/rooms/{roomId}
```

#### 4. 更新自习室
```
PUT /api/rooms/{roomId}
Content-Type: application/json

{
  "roomName": "更新后的名称",
  "maxMembers": 30,
  "endTime": 3600000,
  "musicName": "森林鸟鸣"
}
```

#### 5. 解散自习室
```
DELETE /api/rooms/{roomId}?userId=1
```

#### 6. 加入自习室
```
POST /api/rooms/{roomId}/join?userId=1
```

#### 7. 离开自习室
```
POST /api/rooms/{roomId}/leave?userId=1
```

#### 8. 获取自习室成员列表
```
GET /api/rooms/{roomId}/members
```

#### 9. 踢出成员
```
DELETE /api/rooms/{roomId}/members/{userId}?creatorId=1
```

### 任务相关接口

#### 1. 获取当前用户的任务
```
GET /api/me/tasks?userId=1
```

#### 2. 创建新任务
```
POST /api/tasks
Content-Type: application/json

{
  "userId": 1,
  "taskName": "学习Vue3",
  "taskNote": "学习组合式API",
  "duration": 25
}
```

#### 3. 更新任务
```
PUT /api/tasks/{taskId}
Content-Type: application/json

{
  "taskId": 1,
  "userId": 1,
  "taskName": "学习Vue3",
  "taskNote": "学习组合式API",
  "duration": 30,
  "status": "进行中"
}
```

#### 4. 删除任务
```
DELETE /api/tasks/{taskId}?userId=1
```

### 资源相关接口

#### 1. 获取背景音乐列表
```
GET /api/resources/background-music
```

#### 2. 获取系统配置
```
GET /api/system/configs
```

## 📝 统一响应格式

所有API接口返回格式：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

错误响应：

```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

## 🔧 配置说明

### 端口配置

默认端口：8080

可在 `application.yml` 中修改：
```yaml
server:
  port: 8080
```

### 跨域配置

已配置允许所有来源的跨域请求，可在 `CorsConfig.java` 中修改。

### 日志配置

日志级别可在 `application.yml` 中配置：
```yaml
logging:
  level:
    com.tomato.studyroom: debug
```

## 📦 依赖说明

主要依赖：
- `spring-boot-starter-web`: Web框架
- `mybatis-plus-boot-starter`: MyBatis Plus ORM框架
- `mysql-connector-j`: MySQL驱动
- `lombok`: 简化代码
- `spring-boot-starter-validation`: 参数验证

## 🐛 常见问题

1. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 检查数据库用户名密码是否正确
   - 检查数据库是否已创建

2. **端口被占用**
   - 修改 `application.yml` 中的端口号
   - 或关闭占用8080端口的程序

3. **编译错误**
   - 确认JDK版本为17+
   - 运行 `mvn clean install` 重新编译

## 📄 许可证

MIT License

## 👥 贡献

欢迎提交Issue和Pull Request！
