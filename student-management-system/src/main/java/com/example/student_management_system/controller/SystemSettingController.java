package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SystemSettingController {

    @Autowired
    private SystemSettingService systemSettingService;

    // 供学生和管理员检查当前选课通道状态
    @GetMapping("/course-selection-status")
    public ApiResult<Map<String, Boolean>> getCourseSelectionStatus() {
        boolean isOpen = systemSettingService.isCourseSelectionOpen();
        return ApiResult.success(Map.of("isOpen", isOpen));
    }

    // 仅供管理员更新选课通道状态
    @PostMapping("/course-selection-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<?> setCourseSelectionStatus(@RequestBody Map<String, Boolean> payload) {
        Boolean isOpen = payload.get("isOpen");
        if (isOpen == null) {
            return ApiResult.failure(400, "请求体必须包含 'isOpen' 字段。");
        }
        systemSettingService.setCourseSelectionOpen(isOpen);
        return ApiResult.success(Map.of("isOpen", isOpen));
    }

    // 【代码新增】获取排课设置的接口
    @GetMapping("/scheduling")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Map<String, Integer>> getSchedulingSettings() {
        return ApiResult.success(systemSettingService.getSchedulingSettings());
    }

    // 【代码新增】更新排课设置的接口
    @PostMapping("/scheduling")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<?> updateSchedulingSettings(@RequestBody Map<String, Integer> settings) {
        Integer year = settings.get("year");
        Integer semester = settings.get("semester");
        if (year == null || semester == null) {
            return ApiResult.failure(400, "请求体必须包含 'year' 和 'semester' 字段。");
        }
        systemSettingService.updateSchedulingSettings(year, semester);
        return ApiResult.success();
    }
}