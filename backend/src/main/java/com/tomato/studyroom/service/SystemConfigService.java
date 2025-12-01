package com.tomato.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomato.studyroom.entity.SystemConfig;

import java.util.List;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService extends IService<SystemConfig> {
    List<SystemConfig> getAllConfigs();
}
