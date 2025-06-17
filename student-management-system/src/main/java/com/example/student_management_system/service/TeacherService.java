package com.example.student_management_system.service;

import com.example.student_management_system.dto.TeacherDTO;
import com.example.student_management_system.dto.TeacherDetailDTO;
import com.example.student_management_system.mapper.CourseOfferingMapper; // 修正依赖
import com.example.student_management_system.mapper.TeacherMapper;
import com.example.student_management_system.model.CourseOffering;
import com.example.student_management_system.model.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherMapper teacherMapper;
    private final CourseOfferingMapper courseOfferingMapper; // 修正依赖
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public TeacherService(TeacherMapper teacherMapper, CourseOfferingMapper courseOfferingMapper, PasswordEncoder passwordEncoder) {
        this.teacherMapper = teacherMapper;
        this.courseOfferingMapper = courseOfferingMapper; // 修正依赖
        this.passwordEncoder = passwordEncoder;
    }

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
        teacher.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));

        teacherMapper.insert(teacher);
        teacherDTO.setPassword(null); // 返回的数据不应包含密码
        return teacherDTO;
    }

    /**
     * 获取所有教师列表，并附带他们所教授的课程安排信息
     * @return 包含课程安排信息的教师列表
     */
    public List<TeacherDetailDTO> getAllTeachersWithOfferings() {
        List<Teacher> teachers = teacherMapper.findAll();
        if (teachers.isEmpty()) {
            return Collections.emptyList();
        }

        // 一次性获取所有课程安排，避免N+1查询
        List<CourseOffering> allOfferings = courseOfferingMapper.findAllWithDetails();

        // 按教师ID将课程安排分组
        Map<Long, List<String>> offeringsByTeacherId = allOfferings.stream()
                .filter(offering -> offering.getTeacherId() != null)
                .collect(Collectors.groupingBy(
                        CourseOffering::getTeacherId,
                        Collectors.mapping(CourseOffering::getCourseName, Collectors.toList())
                ));

        // 组装最终的DTO列表
        return teachers.stream().map(teacher -> {
            TeacherDetailDTO dto = new TeacherDetailDTO();
            dto.setId(teacher.getId());
            dto.setTeacherId(teacher.getTeacherId());
            dto.setName(teacher.getName());
            dto.setTaughtCourses(offeringsByTeacherId.getOrDefault(teacher.getId(), Collections.emptyList()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 更新教师信息
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

        if (StringUtils.hasText(teacherDTO.getPassword())) {
            teacher.setPassword(passwordEncoder.encode(teacherDTO.getPassword()));
        }

        teacherMapper.update(teacher);
        teacherDTO.setPassword(null);
        return teacherDTO;
    }

    /**
     * 删除教师，并解除其与所有课程安排的关联
     * @param id 教师的数据库ID
     */
    @Transactional
    public void deleteTeacher(Long id) {
        if (teacherMapper.findById(id) == null) {
            throw new ResourceNotFoundException("ID为 " + id + " 的教师不存在，无法删除。");
        }
        // 1. 解除该教师与所有课程安排的关联
        courseOfferingMapper.disassociateTeacherFromOfferings(id);

        // 2. 删除教师记录
        teacherMapper.deleteById(id);
    }
}