# 番茄自习室 API 接口文档

## 目录
- [基础信息](#基础信息)
- [认证说明](#认证说明)
- [统一响应格式](#统一响应格式)
- [接口列表](#接口列表)
  - [1. 认证相关](#1-认证相关)
  - [2. 用户信息](#2-用户信息)
  - [3. 专注相关](#3-专注相关)
  - [4. 任务管理](#4-任务管理)
  - [5. 好友系统](#5-好友系统)
  - [6. 自习室](#6-自习室)
  - [7. 背景音乐](#7-背景音乐)
  - [8. 系统配置](#8-系统配置)

---

## 基础信息

- **服务器地址**: `http://localhost:8090`
- **Context Path**: `/api`
- **完整基础URL**: `http://localhost:8090/api`
- **认证方式**: JWT Token (Bearer Token)

⚠️ **重要提示**: 由于配置了 `context-path: /api`，所有接口URL都需要包含 `/api` 前缀！

---

## 认证说明

### Token 获取
通过登录接口获取 token，后续所有需要认证的接口都需要在请求头中携带：

```
Authorization: Bearer <你的token>
```

### Token 格式
- 请求头格式: `Authorization: Bearer <token>`
- 注意 `Bearer` 后面有一个空格
- Token 有效期为 1 小时（3600000 毫秒）

---

## 统一响应格式

### 标准响应格式
```json
{
  "success": true/false,
  "message": "响应消息",
  "data": { ... }
}
```

### 部分接口的特殊格式
- **隐私设置接口**: 使用 `code/message/data` 格式
- **资产信息接口**: 直接返回数据对象
- **创建任务接口**: `success` 为字符串类型

---

## 接口列表

## 1. 认证相关

### 1.1 用户注册

**接口信息**
- **URL**: `POST /api/api/auth/register` 或 `POST /api/auth/register`
- **认证**: 不需要
- **Content-Type**: `application/json`

**请求体**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "phone": "13881265078",
  "password": "123456"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| email | String | 是 | 邮箱（需符合邮箱格式） |
| phone | String | 是 | 手机号 |
| password | String | 是 | 密码（6-128个字符） |

**成功响应** (200)
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 1001,
    "username": "testuser"
  }
}
```

**失败响应** (400)
```json
{
  "success": false,
  "message": "用户名已存在",
  "data": null
}
```

**curl 示例**
```bash
curl -X POST http://localhost:8090/api/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13881265078",
    "password": "123456"
  }'
```

---

### 1.2 用户登录

**接口信息**
- **URL**: `POST /api/api/auth/login` 或 `POST /api/auth/login`
- **认证**: 不需要
- **Content-Type**: `application/json`

**请求体**
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名、邮箱或手机号（三种方式都支持） |
| password | String | 是 | 密码 |

**成功响应** (200)
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 1001,
    "username": "testuser"
  }
}
```

**失败响应** (401)
```json
{
  "success": false,
  "message": "用户不存在"
}
```

或

```json
{
  "success": false,
  "message": "密码错误"
}
```

**curl 示例**
```bash
curl -X POST http://localhost:8090/api/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

---

### 1.3 用户登出

**接口信息**
- **URL**: `POST /api/api/auth/logout` 或 `POST /api/auth/logout`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求头**
```
Authorization: Bearer <你的token>
```

**请求体**: 无

**成功响应** (200)
```json
{
  "success": true,
  "message": "登出成功",
  "data": null
}
```

**curl 示例**
```bash
curl -X POST http://localhost:8090/api/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 2. 用户信息

### 2.1 获取当前用户信息

**接口信息**
- **URL**: `GET /api/api/user/me` 或 `GET /api/user/me`
- **认证**: 需要 (Bearer Token)

**请求头**
```
Authorization: Bearer <你的token>
```

**成功响应** (200)
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "user_id": 1001,
    "username": "testuser",
    "status": "在线",
    "email": "test@example.com",
    "phone": "13881265078",
    "sex": "男",
    "birthday": 1709107200000,
    "tomato": 0,
    "province": "北京"
  }
}
```

**curl 示例**
```bash
curl -X GET http://localhost:8090/api/api/user/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 2.2 更新当前用户信息

**接口信息**
- **URL**: `PUT /api/me`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "username": "newusername",
  "password": "newpassword123",
  "sex": "女",
  "birthday": 1709107200000,
  "province": "上海"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 否 | 用户名（需检查唯一性） |
| password | String | 否 | 密码（6-128个字符） |
| sex | String | 否 | 性别 |
| birthday | Long | 否 | 生日（时间戳，毫秒） |
| province | String | 否 | 省份 |

**说明**: 所有字段都是可选的，只更新提供的字段。

**成功响应** (200)
```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "user_id": 1001,
    "username": "newusername",
    "status": "在线",
    "email": "test@example.com",
    "phone": "13881265078",
    "sex": "女",
    "birthday": 1709107200000,
    "tomato": 0,
    "province": "上海"
  }
}
```

**curl 示例**
```bash
curl -X PUT http://localhost:8090/api/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "sex": "女",
    "birthday": 1709107200000,
    "province": "上海"
  }'
```

---

### 2.3 获取当前用户隐私设置

**接口信息**
- **URL**: `GET /api/me/privacy`
- **认证**: 需要 (Bearer Token)

**成功响应** (200)
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "show_birthday": "public",
    "show_study_time": "public",
    "show_location": "public",
    "allow_friend_request": true,
    "searchable": true
  }
}
```

**字段说明**
| 字段 | 类型 | 说明 |
|------|------|------|
| show_birthday | String | 'public'（公开）、'friends'（仅好友）、'private'（私密） |
| show_study_time | String | 'public'、'friends'、'private' |
| show_location | String | 'public'、'friends'、'private' |
| allow_friend_request | Boolean | 是否接受好友申请 |
| searchable | Boolean | 是否允许被搜索 |

---

### 2.4 更新当前用户隐私设置

**接口信息**
- **URL**: `PUT /api/me/privacy`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "show_birthday": "friends",
  "show_study_time": "private",
  "show_location": "public",
  "allow_friend_request": true,
  "searchable": false
}
```

**成功响应** (200)
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "show_birthday": "friends",
    "show_study_time": "private",
    "show_location": "public",
    "allow_friend_request": true,
    "searchable": false
  }
}
```

---

### 2.5 获取当前用户资产信息

**接口信息**
- **URL**: `GET /api/me/currency`
- **认证**: 需要 (Bearer Token)

**成功响应** (200)
```json
{
  "user_id": 1001,
  "coins": 100,
  "check_day": 5,
  "updated_at": "2025-12-06"
}
```

**字段说明**
| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | Long | 用户ID |
| coins | Integer | 金币数量 |
| check_day | Integer | 本月签到天数 |
| updated_at | String | 更新时间（格式: YYYY-MM-DD） |

---

### 2.6 获取指定用户信息

**接口信息**
- **URL**: `GET /api/users/{username}`
- **认证**: 需要 (Bearer Token)
- **路径参数**: `username` - 用户名

**成功响应** (200)
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "user_id": 1002,
    "username": "otheruser",
    "status": "在线",
    "sex": "男",
    "birthday": 1709107200000,
    "tomato": 10,
    "province": "北京"
  }
}
```

**说明**: 
- 不返回 `email` 和 `phone`（保护隐私）
- 根据隐私设置过滤显示的信息

**curl 示例**
```bash
curl -X GET http://localhost:8090/api/users/otheruser \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 3. 专注相关

### 3.1 开始专注

**接口信息**
- **URL**: `POST /api/focus`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "task_name": "完成项目文档"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| task_name | String | 是 | 任务名称 |

**成功响应** (200)
```json
{
  "success": true,
  "message": "开始专注成功",
  "data": {
    "session_id": 1001,
    "task_name": "完成项目文档",
    "start_time": "2025-12-06 20:00:00",
    "status": "进行中"
  }
}
```

---

### 3.2 结束专注

**接口信息**
- **URL**: `POST /api/focus/stop`
- **认证**: 需要 (Bearer Token)

**请求体**: 无

**成功响应** (200)
```json
{
  "success": true,
  "message": "结束专注成功",
  "data": {
    "session_id": 1001,
    "duration": 25,
    "end_time": "2025-12-06 20:25:00"
  }
}
```

---

### 3.3 获取专注记录

**接口信息**
- **URL**: `GET /api/focus/report`
- **认证**: 需要 (Bearer Token)

**成功响应** (200)
```json
{
  "success": true,
  "message": "获取成功",
  "data": [
    {
      "session_id": 1001,
      "task_name": "完成项目文档",
      "duration": 25,
      "start_time": "2025-12-06 20:00:00",
      "end_time": "2025-12-06 20:25:00",
      "status": "已完成"
    }
  ]
}
```

---

### 3.4 获取学习报告

**接口信息**
- **URL**: `GET /api/users/reporters?report_type=weekly` 或 `GET /api/users/reporters?report_type=monthly`
- **认证**: 需要 (Bearer Token)
- **查询参数**: `report_type` - 报告类型（'weekly' 或 'monthly'）

**成功响应** (200)
```json
{
  "success": true,
  "message": "获取学习报告成功",
  "data": [
    {
      "id": 1,
      "user_id": 1001,
      "report_type": "weekly",
      "report_date": "2025-12-06",
      "title": "本周学习报告",
      "summary": {
        "total_focus_time": 300,
        "completed_tasks": 5,
        "avg_daily_duration": 42.86
      },
      "share_image_url": "",
      "is_read": 0,
      "created_at": "2025-12-06 20:00:00"
    }
  ]
}
```

---

## 4. 任务管理

### 4.1 创建待办任务

**接口信息**
- **URL**: `POST /api/api/tasks` 或 `POST /api/tasks`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "task_name": "完成项目文档",
  "task_note": "编写API文档",
  "duration": 60
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| task_name | String | 是 | 任务名称 |
| task_note | String | 否 | 任务备注 |
| duration | Integer | 是 | 计划时长（分钟，必须大于0） |

**成功响应** (200)
```json
{
  "success": "true",
  "message": "新建任务成功",
  "data": {
    "task_id": 1001,
    "user_id": 1001,
    "task_name": "完成项目文档",
    "task_note": "编写API文档",
    "duration": 60,
    "actual_duration": 0,
    "status": "未完成",
    "created_at": "2025-12-06 20:00:00"
  }
}
```

**curl 示例**
```bash
curl -X POST http://localhost:8090/api/api/tasks \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "task_name": "完成项目文档",
    "task_note": "编写API文档",
    "duration": 60
  }'
```

---

### 4.2 编辑待办任务

**接口信息**
- **URL**: `PUT /api/api/tasks/edit` 或 `PUT /api/tasks/edit`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "task_name": "更新项目文档",
  "task_note": "更新API文档",
  "duration": 90
}
```

**字段说明**: 所有字段都是可选的，只更新提供的字段。

**成功响应** (200)
```json
{
  "success": true,
  "message": "更新任务成功",
  "task_id": 1001,
  "user_id": 1001,
  "task_name": "更新项目文档",
  "task_note": "更新API文档",
  "duration": 90,
  "updated_at": "2025-12-06"
}
```

---

### 4.3 删除待办任务

**接口信息**
- **URL**: `DELETE /api/api/tasks/delete` 或 `DELETE /api/tasks/delete`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "task_id": 1001
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| task_id | Long | 是 | 任务ID |

**成功响应** (200)
```json
{
  "success": true,
  "message": "删除任务成功",
  "data": null
}
```

**失败响应** (400)
```json
{
  "success": false,
  "message": "任务不存在或无权限删除",
  "data": null
}
```

**curl 示例**
```bash
curl -X DELETE http://localhost:8090/api/api/tasks/delete \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "task_id": 1001
  }'
```

---

## 5. 好友系统

### 5.1 发送好友申请

**接口信息**
- **URL**: `POST /api/api/friends/requests` 或 `POST /api/friends/requests`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "user_name": "otheruser",
  "message": "你好，我想加你为好友"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| user_name | String | 是 | 接收好友申请的用户名 |
| message | String | 否 | 申请留言 |

**成功响应** (200)
```json
{
  "success": true,
  "message": "申请成功",
  "data": null
}
```

**失败响应** (400)
```json
{
  "success": false,
  "message": "用户不存在"
}
```

---

### 5.2 查看好友申请

**接口信息**
- **URL**: `GET /api/api/friends/requests` 或 `GET /api/friends/requests`
- **认证**: 需要 (Bearer Token)

**成功响应** (200)
```json
{
  "success": true,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "from_user_id": 1002,
      "from_user_name": "otheruser",
      "to_user_id": 1001,
      "to_user_name": "testuser",
      "status": "待处理",
      "message": "你好，我想加你为好友"
    }
  ]
}
```

**状态说明**
- `待处理` - 等待处理
- `已同意` - 已同意申请
- `已拒绝` - 已拒绝申请

---

### 5.3 处理好友申请

**接口信息**
- **URL**: `PUT /api/api/friends/requests` 或 `PUT /api/friends/requests`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "from_user_id": 1002,
  "action": "accept"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| from_user_id | Long | 是 | 发送好友申请的用户ID |
| action | String | 是 | 操作类型：'accept'（同意）或 'reject'（拒绝） |

**成功响应** (200)
```json
{
  "success": true,
  "message": "处理成功",
  "data": null
}
```

**失败响应** (400)
```json
{
  "success": false,
  "message": "好友申请不存在"
}
```

或

```json
{
  "success": false,
  "message": "该好友申请已处理"
}
```

**curl 示例**
```bash
# 同意好友申请
curl -X PUT http://localhost:8090/api/api/friends/requests \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "from_user_id": 1002,
    "action": "accept"
  }'

# 拒绝好友申请
curl -X PUT http://localhost:8090/api/api/friends/requests \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "from_user_id": 1002,
    "action": "reject"
  }'
```

---

### 5.4 获取好友列表

**接口信息**
- **URL**: `GET /api/api/friends` 或 `GET /api/friends`
- **认证**: 需要 (Bearer Token)

**成功响应** (200)
```json
{
  "success": true,
  "message": "获取成功",
  "data": [
    {
      "friend_id": 1002,
      "friend_name": "otheruser",
      "friend_status": "在线"
    }
  ]
}
```

**字段说明**
| 字段 | 类型 | 说明 |
|------|------|------|
| friend_id | Long | 好友用户ID |
| friend_name | String | 好友用户名 |
| friend_status | String | 好友状态（'在线'、'离线'、'专注中'） |

---

### 5.5 删除好友

**接口信息**
- **URL**: `POST /api/api/friends/delete` 或 `DELETE /api/api/friends/delete`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "friend_name": "otheruser"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| friend_name | String | 是 | 要删除的好友用户名 |

**成功响应** (200)
```json
{
  "success": true,
  "message": "删除好友成功",
  "data": null
}
```

**curl 示例**
```bash
curl -X POST http://localhost:8090/api/api/friends/delete \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "friend_name": "otheruser"
  }'
```

---

## 6. 自习室

### 6.1 获取自习室列表

**接口信息**
- **URL**: `GET /api/api/rooms`
- **认证**: 需要 (Bearer Token)

**成功响应** (200)
```json
{
  "success": true,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "room_id": 1001,
      "room_name": "学习小组1",
      "create_person": 1001,
      "max_members": 20,
      "end_time": 1709107200000,
      "music_id": 1,
      "music_name": "轻音乐",
      "created_at": "2025-12-06 20:00:00",
      "updated_at": "2025-12-06 20:00:00"
    }
  ]
}
```

---

### 6.2 创建自习室

**接口信息**
- **URL**: `POST /api/api/rooms`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

**请求体**
```json
{
  "roomName": "学习小组1",
  "createPerson": 1001,
  "maxMembers": 20,
  "endTime": 1709107200000,
  "musicName": "轻音乐"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roomName | String | 是 | 房间名称 |
| createPerson | Long | 是 | 创建者用户ID |
| maxMembers | Integer | 是 | 最大人数 |
| endTime | Long | 否 | 房间结束时间（毫秒） |
| musicName | String | 否 | 背景音乐名称 |

---

### 6.3 获取单个自习室

**接口信息**
- **URL**: `GET /api/api/rooms/{roomId}`
- **认证**: 需要 (Bearer Token)
- **路径参数**: `roomId` - 房间ID

---

### 6.4 更新自习室

**接口信息**
- **URL**: `PUT /api/api/rooms/{roomId}`
- **认证**: 需要 (Bearer Token)
- **Content-Type**: `application/json`

---

### 6.5 解散自习室

**接口信息**
- **URL**: `DELETE /api/api/rooms/{roomId}?userId=1001`
- **认证**: 需要 (Bearer Token)
- **查询参数**: `userId` - 创建者用户ID（用于验证权限）

---

### 6.6 加入自习室

**接口信息**
- **URL**: `POST /api/api/rooms/{roomId}/join?userId=1001`
- **认证**: 需要 (Bearer Token)
- **查询参数**: `userId` - 用户ID

---

### 6.7 离开自习室

**接口信息**
- **URL**: `POST /api/api/rooms/{roomId}/leave?userId=1001`
- **认证**: 需要 (Bearer Token)
- **查询参数**: `userId` - 用户ID

---

### 6.8 获取自习室成员列表

**接口信息**
- **URL**: `GET /api/api/rooms/{roomId}/members`
- **认证**: 需要 (Bearer Token)

---

### 6.9 踢出成员

**接口信息**
- **URL**: `DELETE /api/api/rooms/{roomId}/members/{userId}?creatorId=1001`
- **认证**: 需要 (Bearer Token)
- **查询参数**: `creatorId` - 创建者用户ID（用于验证权限）

---

## 7. 背景音乐

### 7.1 获取背景音乐列表

**接口信息**
- **URL**: `GET /api/api/resources/background-music`
- **认证**: 需要 (Bearer Token)

**成功响应** (200)
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "success": true,
    "data": [
      {
        "id": 1,
        "music_name": "轻音乐",
        "audio_url": "http://example.com/music1.mp3",
        "price": 0.00,
        "is_free": 1,
        "duration": 300,
        "created_at": "2025-12-06 20:00:00"
      }
    ]
  }
}
```

---

## 8. 系统配置

### 8.1 获取系统配置

**接口信息**
- **URL**: `GET /api/api/system/configs`
- **认证**: 需要 (Bearer Token)

**成功响应** (200)
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "success": true,
    "data": [
      {
        "id": 1,
        "config_key": "max_room_members",
        "config_value": "50",
        "description": "最大房间人数",
        "created_at": "2025-12-06 20:00:00",
        "updated_at": "2025-12-06 20:00:00"
      }
    ]
  }
}
```

---

## 常见错误码

| HTTP状态码 | 说明 |
|-----------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误或业务逻辑错误 |
| 401 | 未授权（token无效或过期） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 注意事项

1. **URL路径**: 由于配置了 `context-path: /api`，且部分Controller中也定义了 `/api` 前缀，实际URL可能需要包含两个 `/api`（如 `/api/api/auth/login`）

2. **Token格式**: 必须是 `Bearer <token>` 格式，注意Bearer后面有空格

3. **Content-Type**: POST/PUT/DELETE请求必须设置 `Content-Type: application/json`

4. **字段验证**: 所有带 `@NotBlank`、`@NotNull` 等注解的字段都是必填的

5. **时间格式**: 
   - 时间戳使用毫秒（如：1709107200000）
   - 日期字符串格式：`yyyy-MM-dd` 或 `yyyy-MM-dd HH:mm:ss`

6. **隐私保护**: 获取指定用户信息时，不会返回 `email` 和 `phone`

7. **状态管理**: 
   - 注册时用户状态为"离线"
   - 登录后状态更新为"在线"
   - 登出后状态更新为"离线"

---

## 测试工具推荐

1. **Postman** - 图形化界面，支持环境变量
2. **curl** - 命令行工具
3. **Apifox** - 国产API测试工具
4. **Insomnia** - 轻量级API客户端

---

## 快速测试脚本

### PowerShell 测试脚本

```powershell
# 设置基础URL
$baseUrl = "http://localhost:8090/api"
$token = ""

# 1. 注册
$registerBody = @{
    username = "testuser"
    email = "test@example.com"
    phone = "13881265078"
    password = "123456"
} | ConvertTo-Json

$registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body $registerBody

$token = $registerResponse.data.token
Write-Host "Token: $token"

# 2. 登录
$loginBody = @{
    username = "testuser"
    password = "123456"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
    -Method POST `
    -ContentType "application/json" `
    -Body $loginBody

$token = $loginResponse.data.token

# 3. 获取用户信息
$headers = @{
    Authorization = "Bearer $token"
}

$userResponse = Invoke-RestMethod -Uri "$baseUrl/api/user/me" `
    -Method GET `
    -Headers $headers

Write-Host "用户信息: $($userResponse | ConvertTo-Json)"
```

---

## 更新日志

- **2025-12-06**: 初始版本
  - 完成所有接口文档
  - 修复数据库字段映射问题
  - 优化接口响应格式

---

## 联系方式

如有问题或建议，请联系开发团队。

