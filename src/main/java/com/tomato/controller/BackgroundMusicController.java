package com.tomato.controller;

import com.tomato.common.ApiResponse;
import com.tomato.entity.BackgroundMusic;
import com.tomato.service.BackgroundMusicService;
import org.springframework.http.ResponseEntity;
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

    private final BackgroundMusicService backgroundMusicService;

    public BackgroundMusicController(BackgroundMusicService backgroundMusicService) {
        this.backgroundMusicService = backgroundMusicService;
    }

    /**
     * 获取背景音乐列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBackgroundMusic() {
        List<BackgroundMusic> musicList = backgroundMusicService.getAllMusic();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", musicList);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

