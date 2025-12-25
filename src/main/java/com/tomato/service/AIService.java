package com.tomato.service;

import com.tomato.dto.ChatRequest;
import com.tomato.dto.ChatResponse;

/**
 * AI服务接口
 */
public interface AIService {
    /**
     * 与AI对话
     * @param request 聊天请求
     * @param token 用户token（用于扣除番茄）
     * @return AI回复
     */
    ChatResponse chat(ChatRequest request, String token);
}

