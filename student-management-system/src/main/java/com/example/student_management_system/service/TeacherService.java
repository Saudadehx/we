package com.example.student_management_system.service;

import com.example.student_management_system.dto.TeacherDTO;
import com.example.student_management_system.mapper.TeacherMapper;
import com.example.student_management_system.model.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 管理员创建新教师账户
     * @param teacherDTO 包含教师工号、姓名和原始密码的DTO
     * @return 创建后的教师信息（不含密码）
     */
    public TeacherDTO createTeacher(TeacherDTO teacherDTO) {
        if (teacherMapper.findByTeacherId(teacherDTO.getTeacherId()) != null) {
            throw new IllegalArgumentException("教师工号 " + teacherDTO.getTeacherId() + " 已存在。");
        }

        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherDTO.getTeacherId());
        teacher.setName(teacherDTO.getName());
        // 核心业务：密码必须加密存储
        teacher.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));

        teacherMapper.insert(teacher);

        // 返回的数据不应包含密码
        teacherDTO.setPassword(null);
        return teacherDTO;
    }

    /**
     * 获取所有教师列表，用于管理员界面展示
     * @return 教师信息列表（不含密码）
     */
    public List<TeacherDTO> getAllTeachers() {
        return teacherMapper.findAll().stream().map(teacher -> {
            TeacherDTO dto = new TeacherDTO();
            dto.setTeacherId(teacher.getTeacherId());
            dto.setName(teacher.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    // 此处可以添加更新和删除教师的逻辑
}