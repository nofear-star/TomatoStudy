package com.tomato.studyroom.controller;

import com.tomato.studyroom.common.Result;
import com.tomato.studyroom.dto.TaskCreateDTO;
import com.tomato.studyroom.dto.TaskDeleteDTO;
import com.tomato.studyroom.dto.TaskUpdateDTO;
import com.tomato.studyroom.entity.Task;
import com.tomato.studyroom.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务控制器
 */
@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 获取当前用户的任务
     */
    @GetMapping("/me/tasks")
    public Result<List<Task>> getMyTasks(@RequestParam Long userId) {
        List<Task> tasks = taskService.getTasksByUserId(userId);
        return Result.success(tasks);
    }

    /**
     * 创建新任务
     */
    @PostMapping("/tasks")
    public Result<Task> createTask(@Valid @RequestBody TaskCreateDTO dto) {
        Task task = taskService.createTask(dto);
        return Result.success(task);
    }

    /**
     * 更新任务
     */
    @PutMapping("/tasks/{taskId}")
    public Result<Task> updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskUpdateDTO dto) {
        Task task = taskService.updateTask(taskId, dto);
        return Result.success(task);
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/tasks/{taskId}")
    public Result<?> deleteTask(@PathVariable Long taskId, @Valid @RequestBody TaskDeleteDTO dto) {
        if (!dto.getTaskId().equals(taskId)) {
            throw new IllegalArgumentException("路径参数与请求体中的taskId不一致");
        }
        taskService.deleteTask(dto.getTaskId(), dto.getUserId());
        return Result.success("删除成功", null);
    }
}
