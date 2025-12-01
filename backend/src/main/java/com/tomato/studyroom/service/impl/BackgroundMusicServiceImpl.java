package com.tomato.studyroom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomato.studyroom.entity.BackgroundMusic;
import com.tomato.studyroom.mapper.BackgroundMusicMapper;
import com.tomato.studyroom.service.BackgroundMusicService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 背景音乐服务实现
 */
@Service
public class BackgroundMusicServiceImpl extends ServiceImpl<BackgroundMusicMapper, BackgroundMusic> implements BackgroundMusicService {

    @Override
    public List<BackgroundMusic> getAllMusic() {
        return list();
    }
}
