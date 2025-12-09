package com.tomato.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomato.entity.SystemConfig;
import com.tomato.mapper.SystemConfigMapper;
import com.tomato.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统配置服务实现
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    @Override
    public List<SystemConfig> getAllConfigs() {
        return list();
    }
}

