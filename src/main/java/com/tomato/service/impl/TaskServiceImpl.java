package com.tomato.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tomato.dto.TaskCreateDTO;
import com.tomato.dto.TaskUpdateDTO;
import com.tomato.entity.Task;
import com.tomato.entity.User;
import com.tomato.mapper.TaskMapper;
import com.tomato.mapper.UserMapper;
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
    
    private final UserMapper userMapper;
    
    public TaskServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

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

        // 检查任务状态是否从未完成变为已完成
        boolean wasCompleted = "已完成".equals(task.getStatus());
        boolean willBeCompleted = dto.getStatus() != null && "已完成".equals(dto.getStatus());
        boolean justCompleted = !wasCompleted && willBeCompleted;

        if (dto.getTaskName() != null) task.setTaskName(dto.getTaskName());
        if (dto.getTaskNote() != null) task.setTaskNote(dto.getTaskNote());
        if (dto.getDuration() != null) task.setDuration(dto.getDuration());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        task.setUpdatedAt(LocalDateTime.now());

        updateById(task);
        
        // 如果任务刚刚完成，给用户增加1个番茄
        if (justCompleted) {
            User user = userMapper.findByUserId(task.getUserId());
            if (user != null) {
                int currentTomatoes = user.getTomato() != null ? user.getTomato() : 0;
                user.setTomato(currentTomatoes + 1);
                userMapper.updateById(user);
            }
        }
        
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

