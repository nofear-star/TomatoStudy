package com.tomato.controller;

import com.tomato.common.ApiResponse;
import com.tomato.dto.*;
import com.tomato.service.FileUploadService;
import com.tomato.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UserController {

    private final UserService userService;
    private final FileUploadService fileUploadService;

    public UserController(UserService userService, FileUploadService fileUploadService) {
        this.userService = userService;
        this.fileUploadService = fileUploadService;
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<UserResponse> resp = userService.getCurrentUser(token);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping(value = "/me", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserRequest req) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<UserResponse> resp = userService.updateCurrentUser(token, req);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 上传用户头像
     */
    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<UserResponse>builder()
                            .success(false)
                            .message("请选择要上传的文件")
                            .build()
            );
        }

        ApiResponse<UserResponse> resp = userService.uploadAvatar(token, file);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 获取当前用户隐私设置
     */
    @GetMapping("/me/privacy")
    public ResponseEntity<?> getCurrentUserPrivacy(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<PrivacyResponse>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<PrivacyResponse> resp = userService.getCurrentUserPrivacy(token);
        
        // 转换为用户要求的格式：code/message/data
        if (resp.isSuccess()) {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("code", 200);
            response.put("message", resp.getMessage());
            response.put("data", resp.getData());
            return ResponseEntity.ok(response);
        } else {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("code", 400);
            response.put("message", resp.getMessage());
            response.put("data", null);
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * 更新当前用户隐私设置
     */
    @PutMapping(value = "/me/privacy", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateCurrentUserPrivacy(
            HttpServletRequest request,
            @Valid @RequestBody UpdatePrivacyRequest req) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<PrivacyResponse>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<PrivacyResponse> resp = userService.updateCurrentUserPrivacy(token, req);
        
        // 转换为用户要求的格式：code/message/data
        if (resp.isSuccess()) {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("code", 200);
            response.put("message", resp.getMessage());
            response.put("data", resp.getData());
            return ResponseEntity.ok(response);
        } else {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("code", 400);
            response.put("message", resp.getMessage());
            response.put("data", null);
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * 获取当前用户资产信息
     */
    @GetMapping("/me/currency")
    public ResponseEntity<?> getCurrentUserCurrency(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<CurrencyResponse>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<CurrencyResponse> resp = userService.getCurrentUserCurrency(token);
        
        if (resp.isSuccess()) {
            // 直接返回数据对象，符合用户要求的格式
            return ResponseEntity.ok(resp.getData());
        } else {
            return ResponseEntity.status(400).body(
                    ApiResponse.<CurrencyResponse>builder()
                            .success(false)
                            .message(resp.getMessage())
                            .build()
            );
        }
    }

    /**
     * 获取指定用户信息（根据隐私设置过滤）
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<ApiResponse<PublicUserResponse>> getUserByUsername(
            @PathVariable String username,
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<PublicUserResponse>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<PublicUserResponse> resp = userService.getUserByUsername(token, username);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 404).body(resp);
    }

    /**
     * 开始一次专注
     */
    @PostMapping(value = "/focus", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse<FocusResponse>> startFocus(
            HttpServletRequest request,
            @Valid @RequestBody StartFocusRequest req) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<FocusResponse>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<FocusResponse> resp = userService.startFocus(token, req);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 结束专注
     */
    @PostMapping(value = "/focus/stop", produces = "application/json")
    public ResponseEntity<ApiResponse<StopFocusResponse>> stopFocus(
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<StopFocusResponse>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<StopFocusResponse> resp = userService.stopFocus(token);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 获取专注记录
     */
    @GetMapping(value = "/focus/report", produces = "application/json")
    public ResponseEntity<ApiResponse<java.util.List<FocusRecordResponse>>> getFocusReport(
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<java.util.List<FocusRecordResponse>>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<java.util.List<FocusRecordResponse>> resp = userService.getFocusReport(token);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 获取学习报告
     * @param report_type 报告类型，weekly（周报告）或 monthly（月报告）
     */
    @GetMapping(value = "/users/reporters", produces = "application/json")
    public ResponseEntity<ApiResponse<java.util.List<UserReportResponse>>> getUserReports(
            HttpServletRequest request,
            @RequestParam("report_type") String reportType) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<java.util.List<UserReportResponse>>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<java.util.List<UserReportResponse>> resp = userService.getUserReports(token, reportType);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 创建待办任务
     */
    @PostMapping(value = "/tasks", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createTask(
            HttpServletRequest request,
            @Valid @RequestBody CreateTaskRequest req) {
        String token = extractToken(request);
        if (token == null) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", "false");
            errorResponse.put("message", "缺少 Authorization 头，格式应为: Bearer <token>");
            errorResponse.put("data", null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ApiResponse<TaskResponse> resp = userService.createTask(token, req);
        
        // 转换为用户要求的格式：success 为字符串
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", resp.isSuccess() ? "true" : "false");
        response.put("message", resp.getMessage());
        response.put("data", resp.getData());
        
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(response);
    }

    /**
     * 编辑待办任务
     */
    @PutMapping(value = "/tasks/edit", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateTask(
            HttpServletRequest request,
            @Valid @RequestBody UpdateTaskRequest req) {
        String token = extractToken(request);
        if (token == null) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "缺少 Authorization 头，格式应为: Bearer <token>");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        java.util.Map<String, Object> resp = userService.updateTask(token, req);
        
        return ResponseEntity.status((Boolean) resp.get("success") ? 200 : 400).body(resp);
    }

    /**
     * 删除待办任务
     */
    @DeleteMapping(value = "/tasks/delete", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            HttpServletRequest request,
            @Valid @RequestBody DeleteTaskRequest req) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<Void> resp = userService.deleteTask(token, req);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 发送好友申请
     */
    @PostMapping(value = "/friends/requests", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> sendFriendRequest(
            HttpServletRequest request,
            @Valid @RequestBody FriendRequestRequest req) {
        String token = extractToken(request);
        if (token == null) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "缺少 Authorization 头，格式应为: Bearer <token>");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ApiResponse<Void> resp = userService.sendFriendRequest(token, req);
        
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 查看好友申请
     */
    @GetMapping(value = "/friends/requests", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ApiResponse<java.util.List<FriendRequestResponse>>> getFriendRequests(
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<java.util.List<FriendRequestResponse>>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<java.util.List<FriendRequestResponse>> resp = userService.getFriendRequests(token);
        
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 处理好友申请
     */
    @PutMapping(value = "/friends/requests", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> processFriendRequest(
            HttpServletRequest request,
            @Valid @RequestBody ProcessFriendRequestRequest req) {
        String token = extractToken(request);
        if (token == null) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "缺少 Authorization 头，格式应为: Bearer <token>");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ApiResponse<Void> resp = userService.processFriendRequest(token, req);
        
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 获取好友列表
     */
    @GetMapping(value = "/friends", produces = "application/json")
    public ResponseEntity<ApiResponse<java.util.List<FriendResponse>>> getFriends(
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<java.util.List<FriendResponse>>builder()
                            .success(false)
                            .message("缺少 Authorization 头，格式应为: Bearer <token>")
                            .build()
            );
        }

        ApiResponse<java.util.List<FriendResponse>> resp = userService.getFriends(token);
        
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 删除好友
     */
    @RequestMapping(value = "/friends/delete", method = {RequestMethod.POST, RequestMethod.DELETE}, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> deleteFriend(
            HttpServletRequest request,
            @Valid @RequestBody DeleteFriendRequest req) {
        String token = extractToken(request);
        if (token == null) {
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "缺少 Authorization 头，格式应为: Bearer <token>");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ApiResponse<Void> resp = userService.deleteFriend(token, req);
        
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    /**
     * 从请求头中提取 token
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank() || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}

