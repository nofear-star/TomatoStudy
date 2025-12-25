package com.tomato.common;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import java.sql.SQLException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(400).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message("验证失败: " + errors)
                        .build()
        );
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(400).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message("数据验证失败: " + ex.getMessage())
                        .build()
        );
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("birthday")) {
            return ResponseEntity.status(400).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("birthday 字段格式错误，应为时间戳（毫秒），例如: 1709107200000")
                            .build()
            );
        }
        return ResponseEntity.status(400).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message("请求体格式错误: " + (message != null ? message : "无法解析 JSON"))
                        .build()
        );
    }
    
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        String contentType = ex.getContentType() != null ? ex.getContentType().toString() : "未知";
        return ResponseEntity.status(415).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message("不支持的 Content-Type: " + contentType + "，请使用 application/json")
                        .build()
        );
    }
    
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Void>> handleSQLException(SQLException ex) {
        ex.printStackTrace();
        return ResponseEntity.status(500).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message("数据库错误: " + ex.getMessage())
                        .build()
        );
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ex.printStackTrace();
        String message = ex.getMessage();
        String friendlyMessage = "数据完整性错误";
        
        // 检查是否是唯一性约束冲突
        if (message != null) {
            if (message.contains("Duplicate entry") && message.contains("uk_phone")) {
                friendlyMessage = "手机号已被注册";
            } else if (message.contains("Duplicate entry") && message.contains("uk_username")) {
                friendlyMessage = "用户名已被使用";
            } else if (message.contains("Duplicate entry") && message.contains("uk_email")) {
                friendlyMessage = "邮箱已被注册";
            } else if (message.contains("Duplicate entry")) {
                friendlyMessage = "数据已存在，请检查输入信息";
            } else if (message.contains("doesn't have a default value")) {
                friendlyMessage = "缺少必填字段，请检查输入信息";
            } else {
                friendlyMessage = "数据完整性错误: " + message;
            }
        }
        
        return ResponseEntity.status(400).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message(friendlyMessage)
                        .build()
        );
    }
    
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKey(DuplicateKeyException ex) {
        ex.printStackTrace();
        String message = ex.getMessage();
        String friendlyMessage = "数据已存在";
        
        if (message != null) {
            if (message.contains("uk_phone")) {
                friendlyMessage = "手机号已被注册";
            } else if (message.contains("uk_username")) {
                friendlyMessage = "用户名已被使用";
            } else if (message.contains("uk_email")) {
                friendlyMessage = "邮箱已被注册";
            }
        }
        
        return ResponseEntity.status(400).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message(friendlyMessage)
                        .build()
        );
    }
    
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPathVariable(MissingPathVariableException ex) {
        return ResponseEntity.status(400).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message("缺少路径参数: " + ex.getVariableName())
                        .build()
        );
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(404).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message("接口不存在: " + ex.getRequestURL())
                        .build()
        );
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handle(Exception ex) {
        ex.printStackTrace();
        String errorMsg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        String className = ex.getClass().getSimpleName();
        return ResponseEntity.status(500).body(
                ApiResponse.<Void>builder()
                        .success(false)
                        .message("服务器内部错误: " + className + " - " + errorMsg)
                        .build()
        );
    }
}

