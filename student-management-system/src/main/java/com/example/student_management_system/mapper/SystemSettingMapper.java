package com.example.student_management_system.mapper;

import com.example.student_management_system.model.SystemSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SystemSettingMapper {
    SystemSetting findByKey(@Param("settingKey") String settingKey);
    void updateValue(@Param("settingKey") String settingKey, @Param("settingValue") String settingValue);
}