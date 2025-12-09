package com.tomato.controller;

import com.tomato.common.ApiResponse;
import com.tomato.dto.*;
import com.tomato.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

