package com.tomato.controller;

import com.tomato.common.ApiResponse;
import com.tomato.entity.SystemConfig;
import com.tomato.service.SystemConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/system/configs")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    /**
     * 获取系统配置
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", configs);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

