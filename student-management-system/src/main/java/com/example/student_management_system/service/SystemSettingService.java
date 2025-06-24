package com.example.student_management_system.service;

import com.example.student_management_system.mapper.SystemSettingMapper;
import com.example.student_management_system.model.SystemSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class SystemSettingService {

    @Autowired
    private SystemSettingMapper systemSettingMapper;

    @Transactional(readOnly = true)
    public boolean isCourseSelectionOpen() {
        SystemSetting setting = systemSettingMapper.findByKey("course_selection_open");
        // 如果设置不存在或值为 "true"，则认为开启
        return setting != null && "true".equalsIgnoreCase(setting.getSettingValue());
    }

    @Transactional
    public void setCourseSelectionOpen(boolean isOpen) {
        systemSettingMapper.updateValue("course_selection_open", String.valueOf(isOpen));
    }

    // 【代码新增】获取当前的排课学年
    @Transactional(readOnly = true)
    public int getCurrentAcademicYear() {
        SystemSetting setting = systemSettingMapper.findByKey("current_academic_year");
        if (setting == null || setting.getSettingValue() == null) {
            // 提供一个安全默认值，并警告
            // 在实际生产中，这里应该抛出配置缺失的异常
            return 2024;
        }
        return Integer.parseInt(setting.getSettingValue());
    }

    // 【代码新增】获取当前的排课学期
    @Transactional(readOnly = true)
    public int getCurrentSemester() {
        SystemSetting setting = systemSettingMapper.findByKey("current_semester");
        if (setting == null || setting.getSettingValue() == null) {
            return 1; // 默认第一学期
        }
        return Integer.parseInt(setting.getSettingValue());
    }

    // 【代码新增】一次性获取所有排课相关的设置
    @Transactional(readOnly = true)
    public Map<String, Integer> getSchedulingSettings() {
        return Map.of(
                "year", getCurrentAcademicYear(),
                "semester", getCurrentSemester()
        );
    }

    // 【代码新增】更新排课设置
    @Transactional
    public void updateSchedulingSettings(int year, int semester) {
        systemSettingMapper.updateValue("current_academic_year", String.valueOf(year));
        systemSettingMapper.updateValue("current_semester", String.valueOf(semester));
    }
}