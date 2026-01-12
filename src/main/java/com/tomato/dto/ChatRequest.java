package com.tomato.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * AI聊天请求DTO
 */
@Data
public class ChatRequest {
    /**
     * 消息列表
     */
    private List<Message> messages;
    
    /**
     * 是否扣除番茄（默认true，打开聊天时扣除，发送消息时不扣除）
     */
    private Boolean deductTomato = true;
    
    /**
     * 学习上下文数据（任务列表、番茄统计等）
     */
    private Map<String, Object> context;
    
    @Data
    public static class Message {
        /**
         * 角色：user 或 assistant
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
    }
}

