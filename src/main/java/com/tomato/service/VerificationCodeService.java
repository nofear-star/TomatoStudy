package com.tomato.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

/**
 * 验证码服务
 * 使用内存存储验证码（生产环境建议使用Redis）
 */
@Service
public class VerificationCodeService {
    
    // 存储邮箱和验证码的映射，格式：email -> {code, expireTime}
    private final Map<String, CodeInfo> codeStorage = new ConcurrentHashMap<>();
    
    // 验证码有效期（5分钟）
    private static final long CODE_EXPIRE_TIME = 5 * 60 * 1000;
    
    // 验证码长度
    private static final int CODE_LENGTH = 6;
    
    /**
     * 生成并存储验证码
     */
    public String generateAndStoreCode(String email) {
        // 生成6位数字验证码
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        
        String codeStr = code.toString();
        long expireTime = System.currentTimeMillis() + CODE_EXPIRE_TIME;
        
        // 存储验证码
        codeStorage.put(email, new CodeInfo(codeStr, expireTime));
        
        return codeStr;
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCode(String email, String code) {
        CodeInfo codeInfo = codeStorage.get(email);
        if (codeInfo == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.expireTime) {
            codeStorage.remove(email);
            return false;
        }
        
        // 验证码匹配后删除
        if (codeInfo.code.equals(code)) {
            codeStorage.remove(email);
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查邮箱是否已有验证码（用于防止频繁发送）
     */
    public boolean hasCode(String email) {
        CodeInfo codeInfo = codeStorage.get(email);
        if (codeInfo == null) {
            return false;
        }
        
        // 如果已过期，删除并返回false
        if (System.currentTimeMillis() > codeInfo.expireTime) {
            codeStorage.remove(email);
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证码信息内部类
     */
    private static class CodeInfo {
        String code;
        long expireTime;
        
        CodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
}

