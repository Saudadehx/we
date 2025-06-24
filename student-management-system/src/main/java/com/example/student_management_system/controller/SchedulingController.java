package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/scheduling")
@PreAuthorize("hasRole('ADMIN')") // 只有管理员可以执行此操作
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    @PostMapping("/generate")
    public ApiResult<?> startScheduling() {
        // 调用异步服务
        schedulingService.generateSchedule();

        // 立即返回响应，告诉前端任务已经开始
        return ApiResult.success(Map.of("message", "排课任务已启动，正在后台处理中..."));
    }
}