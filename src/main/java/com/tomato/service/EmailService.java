package com.tomato.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 邮件服务
 * 注意：需要在application.yml中配置邮件服务器信息
 * 如果未配置邮件服务器，验证码将记录到日志（开发环境）
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final String fromEmail;
    
    // 可选注入：如果邮件依赖未加载，此字段为null
    private final Object mailSender;
    
    /**
     * 构造函数注入（推荐方式）
     */
    public EmailService(@Value("${spring.mail.username:}") String fromEmail,
                       org.springframework.beans.factory.BeanFactory beanFactory) {
        this.fromEmail = fromEmail;
        // 尝试从Spring容器获取邮件发送器（如果存在）
        Object mailSenderTemp = null;
        try {
            mailSenderTemp = beanFactory.getBean(Class.forName("org.springframework.mail.javamail.JavaMailSender"));
        } catch (Exception e) {
            // 如果邮件依赖未加载或Bean不存在，保持为null
            logger.debug("邮件服务Bean未找到，邮件依赖可能未加载: {}", e.getMessage());
        }
        this.mailSender = mailSenderTemp;
    }
    
    /**
     * 发送验证码邮件（注册用）
     * 注意：如果邮件依赖未加载，将记录到日志
     */
    public void sendVerificationCode(String toEmail, String code) {
        // 尝试发送邮件（如果邮件服务可用）
        boolean emailSent = trySendEmail(toEmail, code, "番茄学习 - 邮箱验证码", 
            "您的验证码是：" + code + "\n\n验证码有效期为5分钟，请勿泄露给他人。\n\n如果这不是您的操作，请忽略此邮件。");
        
        if (emailSent) {
            logger.info("验证码已发送到邮箱: {}", toEmail);
        } else {
            // 开发环境：记录验证码到日志（使用INFO级别，确保能看到）
            logger.info("========================================");
            logger.info("【注册验证码】邮箱: {}", toEmail);
            logger.info("【注册验证码】验证码: {}", code);
            logger.info("【提示】邮件服务器未配置，验证码已记录到日志");
            logger.info("========================================");
        }
    }

    /**
     * 发送重置密码验证码邮件
     * 注意：如果邮件依赖未加载，将记录到日志
     */
    public void sendResetPasswordCode(String toEmail, String code) {
        // 尝试发送邮件（如果邮件服务可用）
        boolean emailSent = trySendEmail(toEmail, code, "番茄学习 - 重置密码验证码",
            "您正在重置密码，验证码是：" + code + "\n\n验证码有效期为5分钟，请勿泄露给他人。\n\n如果这不是您的操作，请立即修改密码。");
        
        if (emailSent) {
            logger.info("重置密码验证码已发送到邮箱: {}", toEmail);
        } else {
            // 开发环境：记录验证码到日志（使用INFO级别，确保能看到）
            logger.info("========================================");
            logger.info("【重置密码验证码】邮箱: {}", toEmail);
            logger.info("【重置密码验证码】验证码: {}", code);
            logger.info("【提示】邮件服务器未配置，验证码已记录到日志");
            logger.info("========================================");
        }
    }
    
    /**
     * 尝试发送邮件
     * @return 是否发送成功
     */
    private boolean trySendEmail(String toEmail, String code, String subject, String text) {
        // 检查邮件配置
        if (fromEmail == null || fromEmail.isEmpty()) {
            return false;
        }
        
        // 检查邮件服务是否可用
        if (mailSender == null) {
            return false;
        }
        
        try {
            // 使用反射调用邮件发送方法（避免编译时依赖）
            Class<?> mailSenderClass = mailSender.getClass();
            Class<?> messageClass = Class.forName("org.springframework.mail.SimpleMailMessage");
            Object message = messageClass.getDeclaredConstructor().newInstance();
            
            messageClass.getMethod("setFrom", String.class).invoke(message, fromEmail);
            messageClass.getMethod("setTo", String.class).invoke(message, toEmail);
            messageClass.getMethod("setSubject", String.class).invoke(message, subject);
            messageClass.getMethod("setText", String.class).invoke(message, text);
            
            mailSenderClass.getMethod("send", messageClass).invoke(mailSender, message);
            return true;
            
        } catch (ClassNotFoundException e) {
            // 邮件依赖未加载
            logger.debug("邮件服务类未找到，邮件依赖可能未加载");
            return false;
        } catch (Exception e) {
            logger.error("发送邮件时发生错误", e);
            return false;
        }
    }
}

