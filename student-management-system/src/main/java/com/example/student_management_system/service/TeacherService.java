package com.example.student_management_system.service;

import com.example.student_management_system.dto.TeacherDTO;
import com.example.student_management_system.mapper.TeacherMapper;
import com.example.student_management_system.model.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 导入
import org.springframework.util.StringUtils; // 导入

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
            // 【新增】返回数据库的ID，方便前端操作
            dto.setId(teacher.getId());
            dto.setName(teacher.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 【新增】更新教师信息
     * @param id 教师的数据库ID
     * @param teacherDTO 包含要更新信息的DTO
     * @return 更新后的教师信息
     */
    @Transactional
    public TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO) {
        Teacher teacher = teacherMapper.findById(id);
        if (teacher == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的教师不存在。");
        }
        teacher.setName(teacherDTO.getName());
        teacher.setTeacherId(teacherDTO.getTeacherId());

        // 如果传入了新密码，则更新密码
        if (StringUtils.hasText(teacherDTO.getPassword())) {
            teacher.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));
        }

        teacherMapper.update(teacher);
        teacherDTO.setPassword(null);
        return teacherDTO;
    }

    /**
     * 【新增】删除教师
     * @param id 教师的数据库ID
     */
    @Transactional
    public void deleteTeacher(Long id) {
        if (teacherMapper.findById(id) == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的教师不存在，无法删除。");
        }
        // 在实际项目中，删除教师前可能还需要检查该教师是否关联了课程
        teacherMapper.deleteById(id);
    }
}