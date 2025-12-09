package com.tomato.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomato.dto.TaskCreateDTO;
import com.tomato.dto.TaskUpdateDTO;
import com.tomato.entity.Task;
import com.tomato.mapper.TaskMapper;
import com.tomato.service.TaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 任务服务实现
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Override
    public List<Task> getTasksByUserId(Long userId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getUserId, userId)
               .orderByDesc(Task::getCreatedAt)
               .orderByDesc(Task::getId);
        return list(wrapper);
    }

    @Override
    @Transactional
    public Task createTask(TaskCreateDTO dto) {
        Task task = new Task();
        BeanUtils.copyProperties(dto, task);
        task.setTaskId(generateBusinessId());
        task.setStatus("未完成");
        task.setActualDuration(task.getActualDuration() == null ? 0 : task.getActualDuration());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        save(task);
        return task;
    }

    @Override
    @Transactional
    public Task updateTask(Long taskId, TaskUpdateDTO dto) {
        Task task = findTaskByBusinessId(taskId);
        if (!task.getUserId().equals(dto.getUserId())) {
            throw new IllegalArgumentException("无权限修改此任务");
        }

        if (dto.getTaskName() != null) task.setTaskName(dto.getTaskName());
        if (dto.getTaskNote() != null) task.setTaskNote(dto.getTaskNote());
        if (dto.getDuration() != null) task.setDuration(dto.getDuration());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        task.setUpdatedAt(LocalDateTime.now());

        updateById(task);
        return task;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = findTaskByBusinessId(taskId);
        if (!task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权限删除此任务");
        }
        removeById(task.getId());
    }

    private Task findTaskByBusinessId(Long taskId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getTaskId, taskId);
        Task task = getOne(wrapper);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        return task;
    }

    private long generateBusinessId() {
        return System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000);
    }
}

