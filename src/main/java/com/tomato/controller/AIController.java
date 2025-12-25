package com.tomato.controller;

import com.tomato.common.ApiResponse;
import com.tomato.dto.ChatRequest;
import com.tomato.dto.ChatResponse;
import com.tomato.service.AIService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI对话控制器
 * 注意：由于 application.yml 中设置了 context-path: /api
 * 所以实际访问路径是 /api/ai/chat
 */
@RestController
@RequestMapping("/ai")
public class AIController {
    
    private final AIService aiService;
    
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }
    
    /**
     * AI对话接口
     */
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @RequestBody ChatRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            // 验证请求
            if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.<ChatResponse>builder()
                                .success(false)
                                .message("消息列表不能为空")
                                .build()
                );
            }
            
            // 提取token
            String token = extractToken(httpRequest);
            
            // 调用AI服务（传入token以便扣除番茄）
            ChatResponse response = aiService.chat(request, token);
            
            return ResponseEntity.ok(
                    ApiResponse.<ChatResponse>builder()
                            .success(true)
                            .data(response)
                            .message("对话成功")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    ApiResponse.<ChatResponse>builder()
                            .success(false)
                            .message("AI对话失败: " + e.getMessage())
                            .build()
            );
        }
    }
    
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

