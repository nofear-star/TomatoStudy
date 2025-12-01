package com.tomato.studyroom.controller;

import com.tomato.studyroom.common.Result;
import com.tomato.studyroom.entity.BackgroundMusic;
import com.tomato.studyroom.service.BackgroundMusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 背景音乐控制器
 */
@RestController
@RequestMapping("/resources/background-music")
public class BackgroundMusicController {

    @Autowired
    private BackgroundMusicService backgroundMusicService;

    /**
     * 获取背景音乐列表
     */
    @GetMapping
    public Result<Map<String, Object>> getBackgroundMusic() {
        List<BackgroundMusic> musicList = backgroundMusicService.getAllMusic();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", musicList);
        return Result.success(result);
    }
}
