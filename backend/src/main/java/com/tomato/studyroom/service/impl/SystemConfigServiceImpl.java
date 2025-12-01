package com.tomato.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomato.studyroom.entity.SystemConfig;
import com.tomato.studyroom.mapper.SystemConfigMapper;
import com.tomato.studyroom.service.SystemConfigService;
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
