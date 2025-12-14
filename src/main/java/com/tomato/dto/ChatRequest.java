package com.tomato.dto;

import lombok.Data;
import java.util.List;

/**
 * AI聊天请求DTO
 */
@Data
public class ChatRequest {
    /**
     * 消息列表
     */
    private List<Message> messages;
    
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

