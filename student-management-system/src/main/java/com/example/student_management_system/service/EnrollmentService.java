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

import java.util.Objects;
import java.util.Set;

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
     * 【修改】学生获取自己的所有成绩，并附带课程时间信息
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getEnrollmentsForStudent(Long studentId) {
        List<Enrollment> enrollments = enrollmentMapper.findByStudentId(studentId);
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = enrollments.stream().map(Enrollment::getCourseId).collect(Collectors.toList());
        List<Course> courses = courseMapper.findByIds(courseIds);
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
                // 【新增】填充课程时间
                dto.setCourseDay(course.getCourseDay());
                dto.setCourseTime(course.getCourseTime());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 【新增方法】学生为自己选课
     * @param courseId 要选修课程的数据库ID
     * @param studentId 当前登录学生的数据库ID
     * @return 创建的选课记录
     */
    @Transactional
    public Enrollment enrollCourseForStudent(Long courseId, Long studentId) {
        Course targetCourse = courseMapper.findById(courseId);
        if (targetCourse == null) {
            throw new ResourceNotFoundException("ID为 " + courseId + " 的课程不存在。");
        }

        // --- 时间冲突检测逻辑 ---
        // 1. 获取该生已选的所有课程
        List<Enrollment> enrollments = enrollmentMapper.findByStudentId(studentId);
        if (!enrollments.isEmpty()) {
            List<Long> enrolledCourseIds = enrollments.stream().map(Enrollment::getCourseId).collect(Collectors.toList());
            List<Course> enrolledCourses = courseMapper.findByIds(enrolledCourseIds);

            // 2. 检查时间是否冲突
            for (Course enrolledCourse : enrolledCourses) {
                if (Objects.equals(enrolledCourse.getCourseDay(), targetCourse.getCourseDay()) &&
                        Objects.equals(enrolledCourse.getCourseTime(), targetCourse.getCourseTime())) {
                    // 如果星期和时间段都相同，则判定为冲突
                    throw new IllegalArgumentException("选课失败：与已选课程 '" + enrolledCourse.getCourseName() + "' 时间冲突。");
                }
            }
        }

        // --- 原有逻辑 ---
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setScore(null);

        try {
            enrollmentMapper.insert(enrollment);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("您已选修此课程，请勿重复选择。");
        }
        return enrollment;
    }

    /**
     * [管理员方法] 管理员为学生选课
     * @param enrollmentDTO 包含学生学号和课程编号
     * @return 创建的选课记录
     */
    @Transactional
    public Enrollment createEnrollment(EnrollmentDTO enrollmentDTO) {
        Student student = studentMapper.findByStudentId(enrollmentDTO.getStudentId());
        if (student == null) {
            throw new ResourceNotFoundException("学号为 " + enrollmentDTO.getStudentId() + " 的学生不存在。");
        }

        Course course = courseMapper.findByCourseId(enrollmentDTO.getCourseId());
        if (course == null) {
            throw new ResourceNotFoundException("课程编号为 " + enrollmentDTO.getCourseId() + " 的课程不存在。");
        }

        return enrollCourseForStudent(course.getId(), student.getId());
    }
}