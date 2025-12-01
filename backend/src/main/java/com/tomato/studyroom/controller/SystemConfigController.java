package com.tomato.studyroom.controller;

import com.tomato.studyroom.common.Result;
import com.tomato.studyroom.entity.SystemConfig;
import com.tomato.studyroom.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取系统配置
     */
    @GetMapping
    public Result<Map<String, Object>> getSystemConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", configs);
        return Result.success(result);
    }
}
