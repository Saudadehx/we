package com.example.student_management_system.service;

import com.example.student_management_system.dto.EnrollmentDTO;
import com.example.student_management_system.dto.EnrollmentResponseDTO;
import com.example.student_management_system.mapper.CourseMapper;
import com.example.student_management_system.mapper.EnrollmentMapper;
import com.example.student_management_system.mapper.StudentMapper;
import com.example.student_management_system.mapper.TeacherMapper;
import com.example.student_management_system.model.Course;
import com.example.student_management_system.model.Enrollment;
import com.example.student_management_system.model.Student;
import com.example.student_management_system.model.Teacher;
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
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private SystemSettingService systemSettingService; // 新增：注入SystemSettingService

    // ... getEnrollmentsByCourseForTeacher 方法保持不变 ...
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
        List<Student> students = studentMapper.findByIds(studentIds);
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


    // ... updateGrade 方法保持不变 ...
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

    // ... getEnrollmentsForStudent 方法保持不变 ...
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

        Set<Long> teacherIds = courses.stream()
                .map(Course::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<Teacher> teachers = teacherIds.isEmpty() ? Collections.emptyList() : teacherMapper.findByIds(List.copyOf(teacherIds));
        Map<Long, Teacher> teacherMap = teachers.stream()
                .collect(Collectors.toMap(Teacher::getId, Function.identity()));

        return enrollments.stream().map(enrollment -> {
            EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
            dto.setEnrollmentId(enrollment.getId());
            dto.setScore(enrollment.getScore());
            Course course = courseMap.get(enrollment.getCourseId());
            if (course != null) {
                dto.setCourseName(course.getCourseName());
                dto.setCourseId(course.getCourseId());
                dto.setCredits(course.getCredits());
                dto.setCourseDay(course.getCourseDay());
                dto.setCourseTime(course.getCourseTime());

                Teacher teacher = teacherMap.get(course.getTeacherId());
                dto.setTeacherName(teacher != null ? teacher.getName() : "未知教师");
            }
            return dto;
        }).collect(Collectors.toList());
    }


    /**
     * 学生为自己选课，增加了对选课通道状态的检查
     */
    @Transactional
    public Enrollment enrollCourseForStudent(Long courseDbId, Long studentDbId) {
        // 【规则检查1】检查选课通道是否开启
        if (!systemSettingService.isCourseSelectionOpen()) {
            throw new IllegalStateException("当前非选课时间，无法进行操作。");
        }

        Course targetCourse = courseMapper.findById(courseDbId);
        if (targetCourse == null) {
            throw new ResourceNotFoundException("ID为 " + courseDbId + " 的课程不存在。");
        }

        List<Enrollment> enrollments = enrollmentMapper.findByStudentId(studentDbId);
        if (!enrollments.isEmpty()) {
            List<Long> enrolledCourseIds = enrollments.stream().map(Enrollment::getCourseId).collect(Collectors.toList());
            List<Course> enrolledCourses = courseMapper.findByIds(enrolledCourseIds);

            for (Course enrolledCourse : enrolledCourses) {
                if (enrolledCourse.getId().equals(courseDbId)) {
                    throw new IllegalArgumentException("您已选修此课程，请勿重复选择。");
                }
                if (enrolledCourse.getCourseDay() != null && enrolledCourse.getCourseTime() != null &&
                        targetCourse.getCourseDay() != null && targetCourse.getCourseTime() != null &&
                        Objects.equals(enrolledCourse.getCourseDay(), targetCourse.getCourseDay()) &&
                        Objects.equals(enrolledCourse.getCourseTime(), targetCourse.getCourseTime())) {
                    throw new IllegalArgumentException("选课失败：与已选课程 '" + enrolledCourse.getCourseName() + "' 时间冲突。");
                }
            }
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentDbId);
        enrollment.setCourseId(courseDbId);
        enrollment.setScore(null);

        try {
            enrollmentMapper.insert(enrollment);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("您已选修此课程，请勿重复选择。");
        }
        return enrollment;
    }

    // ... createEnrollment 方法保持不变 ...
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


    /**
     * 学生退课，增加了对选课通道和成绩的检查
     */
    @Transactional
    public void dropCourse(Long enrollmentId, Long studentId) {
        // 【规则检查1】检查选课/退课通道是否开启
        if (!systemSettingService.isCourseSelectionOpen()) {
            throw new IllegalStateException("当前非退课时间，无法进行操作。");
        }

        Enrollment enrollment = enrollmentMapper.findById(enrollmentId);
        if (enrollment == null) {
            throw new ResourceNotFoundException("该选课记录不存在。");
        }

        if (!enrollment.getStudentId().equals(studentId)) {
            throw new AccessDeniedException("您无权退选不属于您的课程。");
        }

        // 【规则检查2】检查课程是否已有成绩
        if (enrollment.getScore() != null) {
            throw new IllegalStateException("该课程已有成绩，无法退选。");
        }

        enrollmentMapper.deleteById(enrollmentId);
    }
}