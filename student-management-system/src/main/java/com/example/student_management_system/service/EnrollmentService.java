package com.example.student_management_system.service;

import com.example.student_management_system.dto.CourseResponseDTO;
import com.example.student_management_system.dto.EnrollmentDTO;
import com.example.student_management_system.dto.EnrollmentResponseDTO;
import com.example.student_management_system.mapper.*; // 引入 MajorMapper
import com.example.student_management_system.model.*; // 引入 Major
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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired private EnrollmentMapper enrollmentMapper;
    @Autowired private CourseMapper courseMapper;
    @Autowired private StudentMapper studentMapper;
    @Autowired private TeacherMapper teacherMapper;
    @Autowired private MajorMapper majorMapper; // 注入 MajorMapper
    @Autowired private SystemSettingService systemSettingService;


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
        if (!systemSettingService.isCourseSelectionOpen()) {
            throw new IllegalStateException("当前非选课时间，无法进行操作。");
        }

        Course targetCourse = courseMapper.findById(courseDbId);
        if (targetCourse == null) {
            throw new ResourceNotFoundException("ID为 " + courseDbId + " 的课程不存在。");
        }

        // 检查是否为该专业的必修课，如果是，则不允许学生手动选择
        if ("COMPULSORY".equals(targetCourse.getCourseType())) {
            throw new IllegalArgumentException("【" + targetCourse.getCourseName() + "】是必修课，由系统自动分配，无需手动选择。");
        }

        List<Enrollment> enrollments = enrollmentMapper.findByStudentId(studentDbId);
        if (!enrollments.isEmpty()) {
            List<Long> enrolledCourseIds = enrollments.stream().map(Enrollment::getCourseId).collect(Collectors.toList());
            if (enrolledCourseIds.contains(courseDbId)) {
                throw new IllegalArgumentException("您已选修此课程，请勿重复选择。");
            }
            List<Course> enrolledCourses = courseMapper.findByIds(enrolledCourseIds);
            for (Course enrolledCourse : enrolledCourses) {
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

        Course course = courseMapper.findById(enrollment.getCourseId());
        if (course == null) {
            throw new ResourceNotFoundException("该选课记录关联的课程不存在。");
        }

        // --- 核心规则：检查课程类型 ---
        if ("COMPULSORY".equals(course.getCourseType())) {
            throw new IllegalStateException("【" + course.getCourseName() + "】是必修课，无法退选。");
        }


        if (enrollment.getScore() != null) {
            throw new IllegalStateException("该课程已有成绩，无法退选。");
        }

        enrollmentMapper.deleteById(enrollmentId);
    }
    /**
     * 新增：为特定专业的特定年级的学生，预置本学期的必修课
     * @param majorId 专业ID
     * @param academicYear 学年
     * @param semester 学期
     * @return 成功分配课程的学生数量
     */
    @Transactional
    public int assignCompulsoryCourses(Long majorId, Integer academicYear, Integer semester) {
        // 1. 找出所有符合条件的必修课
        Map<String, Object> courseParams = Map.of(
                "majorId", majorId,
                "academicYear", academicYear,
                "semester", semester,
                "courseType", "COMPULSORY"
        );
        // 为了使用这个Map，我们需要一个支持多条件查询的CourseMapper方法
        // 我们需要去CourseMapper.xml和CourseMapper.java中添加
        List<Course> compulsoryCourses = courseMapper.findAll(courseParams);

        if (compulsoryCourses.isEmpty()) {
            throw new ResourceNotFoundException("未找到该专业、学年、学期的必修课程。");
        }

        // 2. 找出所有符合条件的学生
        // 我们也需要一个新的StudentMapper方法来按专业和学年查找
        Map<String, Object> studentParams = Map.of(
                "majorId", majorId,
                "academicYear", academicYear
        );
        // 去StudentMapper.xml和.java中添加 findByMajorAndYear
        List<Student> students = studentMapper.findAll(studentParams); // 假设findAll支持这些参数

        if (students.isEmpty()) {
            throw new ResourceNotFoundException("未找到该专业、学年的学生。");
        }

        int assignedCount = 0;
        // 3. 为每个学生分配必修课
        for (Student student : students) {
            List<Enrollment> currentEnrollments = enrollmentMapper.findByStudentId(student.getId());
            Set<Long> enrolledCourseIds = currentEnrollments.stream()
                    .map(Enrollment::getCourseId)
                    .collect(Collectors.toSet());

            for (Course course : compulsoryCourses) {
                // 如果学生尚未选择这门课，则为他自动选择
                if (!enrolledCourseIds.contains(course.getId())) {
                    Enrollment newEnrollment = new Enrollment();
                    newEnrollment.setStudentId(student.getId());
                    newEnrollment.setCourseId(course.getId());
                    enrollmentMapper.insert(newEnrollment);
                }
            }
            assignedCount++;
        }
        return assignedCount;
    }

    /**
     * 【新增】获取单个学生的可选课程列表
     * @param studentId 学生的数据库ID
     * @return 过滤后的课程列表
     */
    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getAvailableCoursesForStudent(Long studentId) {
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            return Collections.emptyList();
        }

        // 1. 获取所有课程
        List<Course> allCourses = courseMapper.findAll(Collections.emptyMap());
        if (allCourses.isEmpty()) {
            return Collections.emptyList();
        }

        // 预加载教师和专业信息，避免N+1查询
        List<Teacher> teachers = teacherMapper.findAll();
        Map<Long, Teacher> teacherMap = teachers.stream().collect(Collectors.toMap(Teacher::getId, Function.identity()));
        List<Major> majors = majorMapper.findAll();
        Map<Long, Major> majorMap = majors.stream().collect(Collectors.toMap(Major::getId, Function.identity()));

        // 2. 过滤出符合条件的课程
        List<CourseResponseDTO> availableCourses = allCourses.stream()
                .filter(course -> {
                    // 条件1：课程是选修课
                    boolean isElective = "ELECTIVE".equals(course.getCourseType());
                    // 条件2：课程是该学生对应专业、学年、学期的必修课
                    boolean isCompulsoryForStudent = "COMPULSORY".equals(course.getCourseType())
                            && Objects.equals(course.getMajorId(), student.getMajorId())
                            && Objects.equals(course.getAcademicYear(), student.getAcademicYear())
                            && Objects.equals(course.getSemester(), student.getSemester());
                    return isElective || isCompulsoryForStudent;
                })
                .map(course -> { // 转换为 DTO
                    CourseResponseDTO dto = new CourseResponseDTO();
                    dto.setId(course.getId());
                    dto.setCourseId(course.getCourseId());
                    dto.setCourseName(course.getCourseName());
                    dto.setCredits(course.getCredits());
                    dto.setCourseDay(course.getCourseDay());
                    dto.setCourseTime(course.getCourseTime());
                    dto.setCourseType(course.getCourseType());

                    Teacher teacher = teacherMap.get(course.getTeacherId());
                    dto.setTeacherName(teacher != null ? teacher.getName() : "未知");

                    return dto;
                })
                .collect(Collectors.toList());

        return availableCourses;
    }

    // ... (其他方法保持不变) ...
}
