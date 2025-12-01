# 番茄自习室 API接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **响应格式**: JSON

## 统一响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

### 错误响应
```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

---

## 1. 自习室相关接口

### 1.1 获取自习室列表

**请求**
```
GET /rooms
```

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "roomId": 1,
      "roomName": "学习室1",
      "createPerson": 1,
      "maxMembers": 20,
      "endTime": null,
      "musicName": "宁静雨声",
      "createdAt": "2024-01-15T10:00:00",
      "updatedAt": "2024-01-15T10:00:00"
    }
  ]
}
```

---

### 1.2 创建自习室

**请求**
```
POST /rooms
Content-Type: application/json
```

**请求体**
```json
{
  "roomName": "学习室1",
  "createPerson": 1,
  "maxMembers": 20,
  "endTime": null,
  "musicName": "宁静雨声"
}
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roomName | String | 是 | 房间名称 |
| createPerson | Long | 是 | 创建者ID |
| maxMembers | Integer | 是 | 最大人数 |
| endTime | Long | 否 | 结束时间(毫秒) |
| musicName | String | 否 | 背景音乐名称 |

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "roomId": 1,
    "roomName": "学习室1",
    "createPerson": 1,
      "maxMembers": 20,
    "endTime": null,
    "musicName": "宁静雨声"
  }
}
```

---

### 1.3 获取单个自习室

**请求**
```
GET /rooms/{roomId}
```

**路径参数**
- `roomId`: 房间ID

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "roomId": 1,
    "roomName": "学习室1",
    "createPerson": 1,
      "maxMembers": 20,
    "endTime": null,
    "musicName": "宁静雨声"
  }
}
```

---

### 1.4 更新自习室

**请求**
```
PUT /rooms/{roomId}
Content-Type: application/json
```

**请求体**
```json
{
  "roomName": "更新后的名称",
  "maxMembers": 30,
  "endTime": 3600000,
  "musicName": "森林鸟鸣"
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "roomId": 1,
    "roomName": "更新后的名称",
      "maxMembers": 30,
    "endTime": 3600000,
    "musicName": "森林鸟鸣"
  }
}
```

---

### 1.5 解散自习室

**请求**
```
DELETE /rooms/{roomId}?userId=1
```

**说明**: 只有创建者可以解散自习室

**响应示例**
```json
{
  "code": 200,
  "message": "解散成功",
  "data": null
}
```

---

### 1.6 加入自习室

**请求**
```
POST /rooms/{roomId}/join?userId=1
```

**响应示例**
```json
{
  "code": 200,
  "message": "加入成功",
  "data": null
}
```

---

### 1.7 离开自习室

**请求**
```
POST /rooms/{roomId}/leave?userId=1
```

**响应示例**
```json
{
  "code": 200,
  "message": "离开成功",
  "data": null
}
```

---

### 1.8 获取自习室成员列表

**请求**
```
GET /rooms/{roomId}/members
```

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "userId": 1,
      "username": "张三",
      "avatar": "https://example.com/avatar.jpg",
      "joinedAt": "2024-01-15T10:00:00"
    }
  ]
}
```

---

### 1.9 踢出成员

**请求**
```
DELETE /rooms/{roomId}/members/{userId}?creatorId=1
```

**说明**: 只有房主可以踢出成员

**响应示例**
```json
{
  "code": 200,
  "message": "踢出成功",
  "data": null
}
```

---

## 2. 任务相关接口

### 2.1 获取当前用户的任务

**请求**
```
GET /me/tasks?userId=1
```

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "taskId": 1,
      "userId": 1,
      "taskName": "学习Vue3",
      "taskNote": "学习组合式API",
      "duration": 25,
      "status": "未完成",
      "createdAt": "2024-01-15T10:00:00",
      "updatedAt": "2024-01-15T10:00:00"
    }
  ]
}
```

---

### 2.2 创建新任务

**请求**
```
POST /tasks
Content-Type: application/json
```

**请求体**
```json
{
  "userId": 1,
  "taskName": "学习Vue3",
  "taskNote": "学习组合式API",
  "duration": 25
}
```

**参数说明**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 是 | 用户ID |
| taskName | String | 是 | 任务名称 |
| taskNote | String | 否 | 任务备注 |
| duration | Integer | 否 | 计划时长(分钟) |

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "taskId": 1,
    "userId": 1,
    "taskName": "学习Vue3",
    "taskNote": "学习组合式API",
    "duration": 25,
    "status": "未完成"
  }
}
```

---

### 2.3 更新任务

**请求**
```
PUT /tasks/{taskId}
Content-Type: application/json
```

**请求体**
```json
{
  "taskId": 1,
  "userId": 1,
  "taskName": "学习Vue3",
  "taskNote": "学习组合式API",
  "duration": 30,
  "status": "进行中"
}
```

**状态枚举值**: `未完成`, `进行中`, `已完成`

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "taskId": 1,
    "userId": 1,
    "taskName": "学习Vue3",
    "taskNote": "学习组合式API",
    "duration": 30,
    "status": "进行中"
  }
}
```

---

### 2.4 删除任务

**请求**
```
DELETE /tasks/{taskId}
Content-Type: application/json
```

**请求体**
```json
{
  "taskId": 1,
  "userId": 1
}
```

**响应示例**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 3. 资源相关接口

### 3.1 获取背景音乐列表

**请求**
```
GET /resources/background-music
```

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "success": true,
    "data": [
      {
        "id": 1,
        "musicName": "宁静雨声",
        "audioUrl": "https://example.com/music/rain.mp3",
        "duration": 1800
      }
    ]
  }
}
```

---

## 4. 系统配置接口

### 4.1 获取系统配置

**请求**
```
GET /system/configs
```

**响应示例**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "success": true,
    "data": [
      {
        "id": 1,
        "configKey": "focus_time",
        "configValue": "25",
        "description": "默认专注时长(分钟)"
      }
    ]
  }
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 500 | 服务器错误 |

## 注意事项

1. 所有时间戳使用毫秒数（Long类型）
2. 日期时间格式：`yyyy-MM-dd HH:mm:ss`
3. 房间创建后，创建者会自动加入房间
4. 只有创建者可以解散自习室
5. 只有房主可以踢出成员
6. 只有任务创建者可以更新/删除任务
