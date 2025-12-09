# 后端测试说明

## 问题修复

已修复以下问题：
1. SecurityConfig 路径匹配 - 移除了重复的 `/api` 前缀
2. JwtFilter 路径检查 - 修复了路径匹配逻辑

## 如何测试

### 1. 启动后端
```bash
cd backend
mvn spring-boot:run
```

### 2. 测试注册接口

使用 curl 或 Postman 测试：

```bash
curl -X POST http://localhost:8090/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "123456"
  }'
```

### 3. 测试登录接口

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

## 路径说明

- 后端 context-path: `/api`
- AuthController 路径: `/auth`
- 完整路径: `/api/auth/register` 和 `/api/auth/login`

## 常见问题

1. **Cannot POST /api/auth/register**
   - 确保后端已启动
   - 检查端口是否为 8090
   - 重启后端服务以应用新配置

2. **CORS 错误**
   - 已配置 CORS，允许所有来源
   - 如果仍有问题，检查 CorsConfig.java

3. **401 未授权**
   - 注册和登录接口不需要 token
   - 其他接口需要 Bearer token

