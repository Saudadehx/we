package com.example.student_management_system.service;

import com.example.student_management_system.mapper.SystemSettingMapper;
import com.example.student_management_system.model.SystemSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}