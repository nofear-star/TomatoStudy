package com.tomato.studyroom.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tomato.studyroom.dto.TaskCreateDTO;
import com.tomato.studyroom.dto.TaskUpdateDTO;
import com.tomato.studyroom.entity.Task;

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
