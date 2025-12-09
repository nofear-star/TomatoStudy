package com.tomato.controller;

import com.tomato.common.ApiResponse;
import com.tomato.dto.TaskCreateDTO;
import com.tomato.dto.TaskDeleteDTO;
import com.tomato.dto.TaskUpdateDTO;
import com.tomato.entity.Task;
import com.tomato.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 获取当前用户的任务
     */
    @GetMapping("/me/tasks")
    public ResponseEntity<ApiResponse<List<Task>>> getMyTasks(@RequestParam Long userId) {
        List<Task> tasks = taskService.getTasksByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    /**
     * 创建新任务
     */
    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<Task>> createTask(@Valid @RequestBody TaskCreateDTO dto) {
        Task task = taskService.createTask(dto);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    /**
     * 更新任务
     */
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskUpdateDTO dto) {
        Task task = taskService.updateTask(taskId, dto);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId, @Valid @RequestBody TaskDeleteDTO dto) {
        if (!dto.getTaskId().equals(taskId)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("路径参数与请求体中的taskId不一致"));
        }
        taskService.deleteTask(dto.getTaskId(), dto.getUserId());
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}

