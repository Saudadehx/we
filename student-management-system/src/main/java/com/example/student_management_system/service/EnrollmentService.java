package com.example.student_management_system.service;

import com.example.student_management_system.dto.EnrollmentDTO;
import com.example.student_management_system.dto.EnrollmentResponseDTO;
import com.example.student_management_system.mapper.CourseMapper;
import com.example.student_management_system.mapper.EnrollmentMapper;
import com.example.student_management_system.mapper.StudentMapper;
import com.example.student_management_system.model.Course;
import com.example.student_management_system.model.Enrollment;
import com.example.student_management_system.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentMapper enrollmentMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private StudentMapper studentMapper;

    /**
     * 教师获取其某门课程的学生花名册及成绩
     * @param courseId 课程的数据库ID
     * @param teacherId 当前登录教师的数据库ID
     * @return 学生成绩列表
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getEnrollmentsByCourseForTeacher(Long courseId, Long teacherId) {
        Course course = courseMapper.findById(courseId);
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("您无权访问该课程的信息。");
        }

        List<Enrollment> enrollments = enrollmentMapper.findByCourseId(courseId);
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> studentIds = enrollments.stream().map(Enrollment::getStudentId).collect(Collectors.toList());
        // 优化：仅查询相关的学生
        List<Student> students = studentMapper.findByIds(studentIds); // 假设StudentMapper有findByIds方法
        Map<Long, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        return enrollments.stream().map(enrollment -> {
            EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
            dto.setEnrollmentId(enrollment.getId());
            dto.setScore(enrollment.getScore());
            Student student = studentMap.get(enrollment.getStudentId());
            if (student != null) {
                dto.setStudentName(student.getName());
                dto.setStudentId(student.getStudentId());
            }
            dto.setCourseName(course.getCourseName());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 教师更新成绩
     * @param enrollmentId 选课记录的ID
     * @param score 新的分数
     * @param teacherId 当前登录教师的ID
     */
    @Transactional
    public void updateGrade(Long enrollmentId, Double score, Long teacherId) {
        Enrollment enrollment = enrollmentMapper.findById(enrollmentId);
        if (enrollment == null) {
            throw new ResourceNotFoundException("该选课记录不存在。");
        }
        Course course = courseMapper.findById(enrollment.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("您无权修改该课程的成绩。");
        }
        enrollmentMapper.updateScore(enrollmentId, score);
    }

    /**
     * 学生获取自己的所有成绩
     * @param studentId 当前登录学生的ID
     * @return 成绩列表
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getEnrollmentsForStudent(Long studentId) {
        List<Enrollment> enrollments = enrollmentMapper.findByStudentId(studentId);
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = enrollments.stream().map(Enrollment::getCourseId).collect(Collectors.toList());
        // 优化：仅查询相关的课程
        List<Course> courses = courseMapper.findByIds(courseIds); // 假设CourseMapper有findByIds方法
        Map<Long, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        return enrollments.stream().map(enrollment -> {
            EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
            dto.setEnrollmentId(enrollment.getId());
            dto.setScore(enrollment.getScore());
            Course course = courseMap.get(enrollment.getCourseId());
            if (course != null) {
                dto.setCourseName(course.getCourseName());
                dto.setCourseId(course.getCourseId());
                dto.setCredits(course.getCredits());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * [新增方法] 管理员为学生选课
     * @param enrollmentDTO 包含学生学号和课程编号
     * @return 创建的选课记录
     */
    @Transactional
    public Enrollment createEnrollment(EnrollmentDTO enrollmentDTO) {
        // 1. 根据前端传来的学号，查找学生实体以获取其数据库ID
        Student student = studentMapper.findByStudentId(enrollmentDTO.getStudentId());
        if (student == null) {
            throw new ResourceNotFoundException("学号为 " + enrollmentDTO.getStudentId() + " 的学生不存在。");
        }

        // 2. 根据前端传来的课程编号，查找课程实体以获取其数据库ID
        Course course = courseMapper.findByCourseId(enrollmentDTO.getCourseId());
        if (course == null) {
            throw new ResourceNotFoundException("课程编号为 " + enrollmentDTO.getCourseId() + " 的课程不存在。");
        }

        // 3. 创建一个新的Enrollment实体
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getId());
        enrollment.setCourseId(course.getId());
        enrollment.setScore(null); // 初始成绩为空

        // 4. 插入数据库，并处理可能发生的重复选课错误
        try {
            enrollmentMapper.insert(enrollment);
        } catch (DataIntegrityViolationException e) {
            // 这个异常通常是因为违反了数据库的唯一性约束 (uk_student_course)
            throw new IllegalArgumentException("该学生已选修此课程，请勿重复分配。");
        }

        return enrollment;
    }
}