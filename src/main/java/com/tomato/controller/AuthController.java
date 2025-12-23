package com.tomato.controller;

import com.tomato.common.ApiResponse;
import com.tomato.dto.*;
import com.tomato.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        ApiResponse<AuthResponse> resp = authService.register(req);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        ApiResponse<AuthResponse> resp = authService.login(req);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 401).body(resp);
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder().success(false).message("邮箱不能为空").build()
            );
        }
        ApiResponse<Void> resp = authService.sendVerificationCode(email);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    @PostMapping("/send-reset-password-code")
    public ResponseEntity<ApiResponse<Void>> sendResetPasswordCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder().success(false).message("邮箱不能为空").build()
            );
        }
        ApiResponse<Void> resp = authService.sendResetPasswordCode(email);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String verificationCode = request.get("verificationCode");
        String newPassword = request.get("newPassword");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder().success(false).message("邮箱不能为空").build()
            );
        }
        if (verificationCode == null || verificationCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder().success(false).message("验证码不能为空").build()
            );
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder().success(false).message("新密码不能为空").build()
            );
        }
        if (newPassword.length() < 6 || newPassword.length() > 15) {
            return ResponseEntity.badRequest().body(
                ApiResponse.<Void>builder().success(false).message("密码长度应为6-15位").build()
            );
        }
        
        ApiResponse<Void> resp = authService.resetPassword(email, verificationCode, newPassword);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder().success(false).message("缺少 Authorization 头，格式应为: Bearer <token>").build());
        }
        if (!header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder().success(false).message("Authorization 头格式错误，应为: Bearer <token>，当前值: " + header).build());
        }
        String token = header.substring(7);
        ApiResponse<Void> resp = authService.logout(token);
        return ResponseEntity.status(resp.isSuccess() ? 200 : 400).body(resp);
    }
}

