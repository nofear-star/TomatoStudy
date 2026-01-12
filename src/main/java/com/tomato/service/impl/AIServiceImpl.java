package com.tomato.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomato.dto.ChatRequest;
import com.tomato.dto.ChatResponse;
import com.tomato.entity.User;
import com.tomato.mapper.UserMapper;
import com.tomato.security.JwtUtil;
import com.tomato.service.AIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI服务实现类
 * 调用通义千问API
 */
@Service
public class AIServiceImpl implements AIService {
    
    @Value("${tongyi.api.key:}")
    private String tongyiApiKey;
    
    @Value("${tongyi.api.url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    private String tongyiApiUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    
    public AIServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request, String token) {
        // 只有在需要扣除番茄时才扣除（打开聊天时扣除，发送消息时不扣除）
        Boolean shouldDeduct = request.getDeductTomato();
        if (shouldDeduct == null) {
            shouldDeduct = true; // 默认扣除，保持向后兼容
        }
        
        if (shouldDeduct && token != null && !token.isEmpty()) {
            try {
                Long userId = jwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    User user = userMapper.findByUserId(userId);
                    if (user != null) {
                        int currentTomatoes = user.getTomato() != null ? user.getTomato() : 0;
                        if (currentTomatoes > 0) {
                            user.setTomato(currentTomatoes - 1);
                            userMapper.updateById(user);
                        } else {
                            // 番茄不足，返回错误
                            ChatResponse errorResponse = new ChatResponse();
                            errorResponse.setContent("番茄不足，无法使用聊天功能。请先完成任务或签到获得番茄！");
                            throw new RuntimeException("番茄不足");
                        }
                    }
                }
            } catch (Exception e) {
                // 如果扣除失败，抛出异常
                if (e.getMessage() != null && e.getMessage().contains("番茄不足")) {
                    throw e;
                }
                // 其他错误（如token无效）继续执行，但不扣除番茄
            }
        }
        System.out.println("=== AI服务调用开始 ===");
        System.out.println("API Key原始值: " + (tongyiApiKey != null ? "[" + tongyiApiKey + "]" : "null"));
        System.out.println("API Key长度: " + (tongyiApiKey != null ? tongyiApiKey.length() : 0));
        System.out.println("API Key是否为空: " + (tongyiApiKey == null || tongyiApiKey.isEmpty()));
        System.out.println("API Key是否为默认值: " + (tongyiApiKey != null && tongyiApiKey.equals("your_tongyi_api_key_here")));
        System.out.println("API URL: " + tongyiApiUrl);
        
        // 调试：打印上下文数据
        System.out.println("📋 上下文数据检查:");
        if (request.getContext() != null) {
            System.out.println("  - Context不为空，包含的key: " + request.getContext().keySet());
            if (request.getContext().containsKey("tasks")) {
                Map<String, Object> tasks = (Map<String, Object>) request.getContext().get("tasks");
                System.out.println("  - tasks不为空: " + (tasks != null));
                if (tasks != null && tasks.containsKey("pending")) {
                    List<Map<String, Object>> pendingTasks = (List<Map<String, Object>>) tasks.get("pending");
                    System.out.println("  - 待完成任务数量: " + (pendingTasks != null ? pendingTasks.size() : 0));
                    if (pendingTasks != null && !pendingTasks.isEmpty()) {
                        System.out.println("  - 第一个任务详情: " + pendingTasks.get(0));
                    }
                }
            } else {
                System.out.println("  - ⚠️ Context中没有tasks字段");
            }
        } else {
            System.out.println("  - ⚠️ Context为null");
        }
        
        boolean isApiKeyValid = tongyiApiKey != null 
                && !tongyiApiKey.isEmpty() 
                && !tongyiApiKey.trim().isEmpty()
                && !tongyiApiKey.equals("your_tongyi_api_key_here");
        
        System.out.println("API Key配置状态: " + (isApiKeyValid ? "已配置" : "未配置"));
        
        if (!isApiKeyValid) {
            // 如果没有配置API Key，返回模拟回复
            System.out.println("⚠️ API Key未配置，使用模拟回复");
            return getMockResponse(request);
        }
        
        try {
            System.out.println("🚀 开始调用通义千问API...");
            
            // 判断是否使用兼容模式（compatible-mode）
            boolean isCompatibleMode = tongyiApiUrl.contains("compatible-mode");
            System.out.println("API模式: " + (isCompatibleMode ? "兼容模式（OpenAI格式）" : "标准模式"));
            
            java.util.Map<String, Object> requestBodyMap = new java.util.HashMap<>();
            
            if (isCompatibleMode) {
                // 兼容OpenAI格式的API
                // 构建消息列表
                java.util.List<java.util.Map<String, String>> messagesList = new java.util.ArrayList<>();
                
                // 添加系统消息（包含学习上下文数据）
                String systemPrompt = buildSystemPrompt(request.getContext());
                System.out.println("📤 构建的系统提示词（前500字符）: " + 
                    (systemPrompt.length() > 500 ? systemPrompt.substring(0, 500) + "..." : systemPrompt));
                
                java.util.Map<String, String> systemMessage = new java.util.HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messagesList.add(systemMessage);
                
                // 添加用户消息（保留最近10条）
                List<ChatRequest.Message> recentMessages = request.getMessages().stream()
                        .limit(10)
                        .collect(Collectors.toList());
                
                for (ChatRequest.Message msg : recentMessages) {
                    java.util.Map<String, String> messageMap = new java.util.HashMap<>();
                    messageMap.put("role", msg.getRole());
                    messageMap.put("content", msg.getContent());
                    messagesList.add(messageMap);
                }
                
                requestBodyMap.put("model", "qwen-turbo");
                requestBodyMap.put("messages", messagesList);
                requestBodyMap.put("temperature", 0.7);
                requestBodyMap.put("max_tokens", 500);
            } else {
                // 标准DashScope格式
                // 构建系统提示词（包含学习上下文）
                String systemPrompt = buildSystemPrompt(request.getContext());
                
                // 构建对话历史
                StringBuilder conversationHistory = new StringBuilder(systemPrompt).append("\n\n");
                List<ChatRequest.Message> recentMessages = request.getMessages().stream()
                        .limit(6) // 保留最近6条消息
                        .collect(Collectors.toList());
                
                for (ChatRequest.Message msg : recentMessages) {
                    if ("user".equals(msg.getRole())) {
                        conversationHistory.append("用户：").append(msg.getContent()).append("\n");
                    } else if ("assistant".equals(msg.getRole())) {
                        conversationHistory.append("番茄小助手：").append(msg.getContent()).append("\n");
                    }
                }
                conversationHistory.append("番茄小助手：");
                
                requestBodyMap.put("model", "qwen-turbo");
                
                java.util.Map<String, Object> inputMap = new java.util.HashMap<>();
                inputMap.put("prompt", conversationHistory.toString());
                requestBodyMap.put("input", inputMap);
                
                java.util.Map<String, Object> parametersMap = new java.util.HashMap<>();
                parametersMap.put("temperature", 0.7);
                parametersMap.put("max_tokens", 500);
                parametersMap.put("top_p", 0.8);
                requestBodyMap.put("parameters", parametersMap);
            }
            
            String requestBody = objectMapper.writeValueAsString(requestBodyMap);
            System.out.println("📤 请求体: " + requestBody);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + tongyiApiKey);
            System.out.println("📤 请求URL: " + tongyiApiUrl);
            System.out.println("📤 Authorization头: Bearer " + (tongyiApiKey.length() > 10 ? tongyiApiKey.substring(0, 10) + "..." : tongyiApiKey));
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            System.out.println("📡 发送HTTP请求...");
            ResponseEntity<String> response = restTemplate.exchange(
                    tongyiApiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            
            System.out.println("📥 HTTP状态码: " + response.getStatusCode());
            System.out.println("📥 响应体: " + response.getBody());
            
            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String content = null;
            
            if (isCompatibleMode) {
                // 兼容模式响应格式（OpenAI格式）
                if (jsonNode.has("choices") && jsonNode.get("choices").isArray() && jsonNode.get("choices").size() > 0) {
                    JsonNode firstChoice = jsonNode.get("choices").get(0);
                    if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                        content = firstChoice.get("message").get("content").asText();
                        System.out.println("✅ 从choices[0].message.content获取到内容（兼容模式）");
                    }
                } else {
                    System.out.println("⚠️ 兼容模式响应中没有choices字段，完整响应: " + response.getBody());
                }
            } else {
                // 标准模式响应格式
                if (jsonNode.has("output")) {
                    JsonNode output = jsonNode.get("output");
                    if (output.has("text")) {
                        content = output.get("text").asText();
                        System.out.println("✅ 从output.text获取到内容（标准模式）");
                    } else if (output.has("choices") && output.get("choices").isArray() && output.get("choices").size() > 0) {
                        JsonNode firstChoice = output.get("choices").get(0);
                        if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                            content = firstChoice.get("message").get("content").asText();
                            System.out.println("✅ 从output.choices[0].message.content获取到内容（标准模式）");
                        }
                    }
                } else {
                    System.out.println("⚠️ 标准模式响应中没有output字段，完整响应: " + response.getBody());
                }
            }
            
            if (content != null && !content.isEmpty()) {
                System.out.println("✅ 成功获取AI回复，内容长度: " + content.length());
                ChatResponse chatResponse = new ChatResponse();
                chatResponse.setContent(content.trim());
                return chatResponse;
            } else {
                System.out.println("⚠️ 无法从响应中提取内容，使用模拟回复");
                return getMockResponse(request);
            }
            
        } catch (Exception e) {
            System.err.println("❌ 通义千问API调用失败: " + e.getMessage());
            System.err.println("❌ 异常类型: " + e.getClass().getName());
            e.printStackTrace();
            // 如果API调用失败，返回模拟回复
            return getMockResponse(request);
        } finally {
            System.out.println("=== AI服务调用结束 ===");
        }
    }
    
    /**
     * 构建系统提示词（包含学习上下文数据）
     */
    private String buildSystemPrompt(Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个可爱的番茄桌宠，名字叫番茄小助手。你的性格活泼、友好、鼓励学习。你的主要职责是陪伴用户学习，提供学习建议和鼓励。回答要简洁、温暖、有趣，不要太长。");
        
        // 如果有学习上下文数据，添加到提示词中
        if (context != null && !context.isEmpty()) {
            prompt.append("\n\n【用户学习数据】");
            
            // 用户信息
            if (context.containsKey("user")) {
                Map<String, Object> user = (Map<String, Object>) context.get("user");
                prompt.append("\n用户名: ").append(user.get("username"));
                prompt.append("，当前番茄数: ").append(user.get("tomato"));
            }
            
            // 今日统计
            if (context.containsKey("today")) {
                Map<String, Object> today = (Map<String, Object>) context.get("today");
                prompt.append("\n今日学习数据:");
                prompt.append("\n  - 学习时长: ").append(today.get("studyTime")).append(" 分钟");
                prompt.append("\n  - 完成任务数: ").append(today.get("completedTasks"));
                prompt.append("\n  - 总任务数: ").append(today.get("totalTasks"));
            }
            
            // 待完成任务
            if (context.containsKey("tasks")) {
                Map<String, Object> tasks = (Map<String, Object>) context.get("tasks");
                List<Map<String, Object>> pendingTasks = (List<Map<String, Object>>) tasks.get("pending");
                
                System.out.println("📝 处理待完成任务，数量: " + (pendingTasks != null ? pendingTasks.size() : 0));
                
                if (pendingTasks != null && !pendingTasks.isEmpty()) {
                    prompt.append("\n待完成任务列表:");
                    for (int i = 0; i < Math.min(pendingTasks.size(), 10); i++) {
                        Map<String, Object> task = pendingTasks.get(i);
                        
                        // 尝试多种字段名获取任务名称（支持name, task_name, taskName）
                        String taskName = null;
                        if (task.get("name") != null && !task.get("name").toString().trim().isEmpty()) {
                            taskName = task.get("name").toString().trim();
                        } else if (task.get("task_name") != null && !task.get("task_name").toString().trim().isEmpty()) {
                            taskName = task.get("task_name").toString().trim();
                        } else if (task.get("taskName") != null && !task.get("taskName").toString().trim().isEmpty()) {
                            taskName = task.get("taskName").toString().trim();
                        }
                        
                        // 如果所有字段都为空，使用默认值
                        if (taskName == null || taskName.isEmpty()) {
                            taskName = "未命名任务";
                            System.out.println("  ⚠️ 任务" + (i + 1) + "名称为空，使用默认值");
                        }
                        
                        Object durationObj = task.get("duration");
                        String duration = durationObj != null ? durationObj.toString() : "25";
                        
                        System.out.println("  - 任务" + (i + 1) + ": " + taskName + " (时长: " + duration + "分钟)");
                        System.out.println("    任务原始数据: " + task);
                        
                        prompt.append("\n  ").append(i + 1).append(". ")
                             .append(taskName)
                             .append("（预计").append(duration).append("分钟）");
                        if (task.get("note") != null && !task.get("note").toString().isEmpty()) {
                            prompt.append(" - ").append(task.get("note"));
                        }
                    }
                    System.out.println("✅ 任务列表已添加到提示词");
                } else {
                    System.out.println("⚠️ 待完成任务列表为空");
                    prompt.append("\n待完成任务列表: 暂无待完成任务");
                }
            } else {
                System.out.println("⚠️ Context中没有tasks字段");
            }
            
            prompt.append("\n\n重要提示：当用户询问任务相关问题时，你必须使用上述【待完成任务列表】中的真实任务名称来回答，绝对不要使用'任务A'、'任务B'这样的占位符。如果列表中有任务，请直接使用任务的实际名称。");
            prompt.append("\n\n请根据以上数据，结合用户的问题，给出个性化的学习建议。");
        }
        
        return prompt.toString();
    }
    
    /**
     * 模拟AI回复
     */
    private ChatResponse getMockResponse(ChatRequest request) {
        ChatResponse response = new ChatResponse();
        
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            response.setContent("你好呀！我是番茄小助手🍅，很高兴认识你！");
            return response;
        }
        
        String lastMessage = request.getMessages().get(request.getMessages().size() - 1).getContent().toLowerCase();
        
        // 简单的关键词匹配回复
        if (lastMessage.contains("你好")) {
            response.setContent("你好呀！我是番茄小助手🍅，很高兴认识你！");
        } else if (lastMessage.contains("学习")) {
            response.setContent("学习很重要呢！记得用番茄工作法，保持专注哦！");
        } else if (lastMessage.contains("累")) {
            response.setContent("累了就休息一下吧，劳逸结合才能更好地学习！");
        } else if (lastMessage.contains("加油")) {
            response.setContent("加油！我相信你可以的！💪");
        } else if (lastMessage.contains("任务")) {
            response.setContent("记得完成待办任务哦，完成一个任务就给自己一个小奖励！");
        } else if (lastMessage.contains("签到")) {
            response.setContent("记得每天签到领取番茄哦，坚持就是胜利！");
        } else if (lastMessage.contains("帮助")) {
            response.setContent("我可以陪你聊天，给你学习建议，或者只是听你说话。有什么想聊的吗？");
        } else {
            String[] defaultResponses = {
                    "我在听呢，继续说吧！",
                    "这个问题很有趣呢，让我想想...",
                    "虽然我还不够聪明，但我会努力理解你的意思的！",
                    "你可以说得更详细一点吗？",
                    "我明白了，还有其他问题吗？"
            };
            response.setContent(defaultResponses[(int) (Math.random() * defaultResponses.length)]);
        }
        
        return response;
    }
}

