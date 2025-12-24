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
    
    /**
     * 是否扣除番茄（默认true，打开聊天时扣除，发送消息时不扣除）
     */
    private Boolean deductTomato = true;
    
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

