package com.tomato.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomato.studyroom.entity.BackgroundMusic;

import java.util.List;

/**
 * 背景音乐服务接口
 */
public interface BackgroundMusicService extends IService<BackgroundMusic> {
    List<BackgroundMusic> getAllMusic();
}
