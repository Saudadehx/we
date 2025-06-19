package com.example.student_management_system.service;

import com.example.student_management_system.dto.TeacherDTO;
import com.example.student_management_system.dto.TeacherDetailDTO;
import com.example.student_management_system.mapper.CourseOfferingMapper;
import com.example.student_management_system.mapper.TeacherMapper;
import com.example.student_management_system.model.Teacher;
import org.springframework.beans.BeanUtils; // ✨ 导入 Spring 的 BeanUtils
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TeacherService {

    private final TeacherMapper teacherMapper;
    private final CourseOfferingMapper courseOfferingMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TeacherService(TeacherMapper teacherMapper, CourseOfferingMapper courseOfferingMapper, PasswordEncoder passwordEncoder) {
        this.teacherMapper = teacherMapper;
        this.courseOfferingMapper = courseOfferingMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public TeacherDTO createTeacher(TeacherDTO teacherDTO) {
        if (teacherMapper.findByTeacherId(teacherDTO.getTeacherId()) != null) {
            throw new IllegalArgumentException("教师工号 " + teacherDTO.getTeacherId() + " 已存在。");
        }

        Teacher teacher = new Teacher();
        // ✨ 使用 BeanUtils 简化属性复制
        BeanUtils.copyProperties(teacherDTO, teacher, "id", "password"); // 忽略id和password
        teacher.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));

        teacherMapper.insert(teacher);

        // 创建成功后，返回的DTO不应包含密码
        teacherDTO.setPassword(null);
        // 通过查询返回的对象获取ID，并设置回DTO，确保DTO是完整的
        teacherDTO.setId(teacher.getId());
        return teacherDTO;
    }
    public List<TeacherDetailDTO> getAllTeachersWithOfferings() {
        return teacherMapper.findAllWithCourseNames();
    }

    @Transactional
    public TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO) {
        Teacher teacher = teacherMapper.findById(id);
        if (teacher == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的教师不存在。");
        }

        // 检查更新后的工号是否与其他人冲突
        Teacher conflictTeacher = teacherMapper.findByTeacherId(teacherDTO.getTeacherId());
        if (conflictTeacher != null && !conflictTeacher.getId().equals(id)) {
            throw new IllegalArgumentException("教师工号 " + teacherDTO.getTeacherId() + " 已被其他教师使用。");
        }

        // ✨ 使用 BeanUtils 简化属性复制
        BeanUtils.copyProperties(teacherDTO, teacher, "id", "password");

        // 仅当传入的密码非空时才更新密码
        if (StringUtils.hasText(teacherDTO.getPassword())) {
            teacher.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));
        }

        teacherMapper.update(teacher);
        // 更新成功后，返回的DTO不应包含密码
        teacherDTO.setPassword(null);
        return teacherDTO;
    }

    @Transactional
    public void deleteTeacher(Long id) {
        if (teacherMapper.findById(id) == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的教师不存在，无法删除。");
        }
        // 在删除教师前，解除其与所有课程安排的关联
        courseOfferingMapper.disassociateTeacherFromOfferings(id);
        teacherMapper.deleteById(id);
    }
}