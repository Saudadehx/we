package com.example.student_management_system.service;

import com.example.student_management_system.dto.TeacherDTO;
import com.example.student_management_system.dto.TeacherDetailDTO;
import com.example.student_management_system.mapper.CourseOfferingMapper;
import com.example.student_management_system.mapper.TeacherMapper;
import com.example.student_management_system.model.Teacher;
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
        teacher.setTeacherId(teacherDTO.getTeacherId());
        teacher.setName(teacherDTO.getName());
        teacher.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));

        teacherMapper.insert(teacher);
        teacherDTO.setPassword(null);
        return teacherDTO;
    }

    /**
     * 获取所有教师列表，并附带他们所教授的课程名称字符串
     * @return 包含课程名称字符串的教师列表
     */
    public List<TeacherDetailDTO> getAllTeachersWithOfferings() {
        // ✨ 简化：直接调用一步到位的SQL查询，不再需要在Java中进行数据组装
        return teacherMapper.findAllWithCourseNames();
    }

    @Transactional
    public TeacherDTO updateTeacher(Long id, TeacherDTO teacherDTO) {
        Teacher teacher = teacherMapper.findById(id);
        if (teacher == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的教师不存在。");
        }
        teacher.setName(teacherDTO.getName());
        teacher.setTeacherId(teacherDTO.getTeacherId());

        if (StringUtils.hasText(teacherDTO.getPassword())) {
            teacher.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));
        }

        teacherMapper.update(teacher);
        teacherDTO.setPassword(null);
        return teacherDTO;
    }

    @Transactional
    public void deleteTeacher(Long id) {
        if (teacherMapper.findById(id) == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的教师不存在，无法删除。");
        }
        courseOfferingMapper.disassociateTeacherFromOfferings(id);
        teacherMapper.deleteById(id);
    }
}