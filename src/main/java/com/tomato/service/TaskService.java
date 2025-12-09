package com.tomato.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomato.dto.TaskCreateDTO;
import com.tomato.dto.TaskUpdateDTO;
import com.tomato.entity.Task;

import java.util.List;

/**
 * 任务服务接口
 */
public interface TaskService extends IService<Task> {
    List<Task> getTasksByUserId(Long userId);
    Task createTask(TaskCreateDTO dto);
    Task updateTask(Long taskId, TaskUpdateDTO dto);
    void deleteTask(Long taskId, Long userId);
}

