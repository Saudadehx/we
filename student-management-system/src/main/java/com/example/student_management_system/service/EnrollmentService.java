package com.example.student_management_system.service;

import com.example.student_management_system.dto.EnrollmentDTO;
import com.example.student_management_system.dto.EnrollmentResponseDTO;
import com.example.student_management_system.event.CourseOfferingUpdatedEvent;
import com.example.student_management_system.mapper.*;
import com.example.student_management_system.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;
    private final CourseOfferingMapper courseOfferingMapper;
    private final StudentMapper studentMapper;
    // 【修正】注入新的 OfferingClassLinkMapper
    private final OfferingClassLinkMapper offeringClassLinkMapper;
    private final SystemSettingService systemSettingService;

    @Autowired
    // 【修正】更新构造函数，注入新的 Mapper
    public EnrollmentService(EnrollmentMapper enrollmentMapper, CourseOfferingMapper courseOfferingMapper, StudentMapper studentMapper, OfferingClassLinkMapper offeringClassLinkMapper, SystemSettingService systemSettingService) {
        this.enrollmentMapper = enrollmentMapper;
        this.courseOfferingMapper = courseOfferingMapper;
        this.studentMapper = studentMapper;
        this.offeringClassLinkMapper = offeringClassLinkMapper;
        this.systemSettingService = systemSettingService;
    }

    @Transactional
    public void reconcileEnrollmentsForStudent(Student student) {
        log.info("开始为学生 {} (ID: {}) 同步课程注册记录...", student.getName(), student.getId());

        List<Enrollment> currentEnrollments = enrollmentMapper.findByStudentId(student.getId());

        if (currentEnrollments != null && !currentEnrollments.isEmpty()) {
            log.debug("学生 {} 当前有 {} 条注册记录，开始清理...", student.getName(), currentEnrollments.size());
            for (Enrollment enrollment : currentEnrollments) {
                if (enrollment.getScore() == null) {
                    CourseOffering offering = courseOfferingMapper.findById(enrollment.getCourseOfferingId());
                    if (offering != null) {
                        boolean isCourseOutOfDate = !Objects.equals(offering.getAcademicYear(), student.getAcademicYear()) ||
                                !Objects.equals(offering.getSemester(), student.getSemester());

                        if (isCourseOutOfDate) {
                            log.info("课程 '{}' (Offering ID: {}) 与学生新的学籍 ({}-{}学年, 第{}学期) 不匹配，将自动退选。",
                                    offering.getCourseName(), offering.getId(), student.getAcademicYear(), student.getAcademicYear() + 1, student.getSemester());
                            enrollmentMapper.deleteById(enrollment.getId());
                        }
                    }
                }
            }
        }

        log.info("清理完成，开始为学生 {} 分配新学期的必修课...", student.getName());
        assignCompulsoryCoursesForStudent(student);
        log.info("学生 {} 的课程同步完成。", student.getName());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void assignCourseInNewTransaction(Long offeringDbId, Long studentDbId) {
        // 这个方法体就是为了在一个新事务中执行选课逻辑
        this.enrollCourseForStudentInternal(offeringDbId, studentDbId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCourseOfferingUpdate(CourseOfferingUpdatedEvent event) {
        CourseOffering offering = event.getCourseOffering();
        log.info("接收到课程安排更新事件，ID: {}, 名称: '{}'。准备为符合条件的学生分配必修课。", offering.getId(), offering.getCourseName());

        // 【修正】逻辑调整为基于班级进行学生查找和课程分配
        if (offering.getAssociatedClasses() == null || offering.getAssociatedClasses().isEmpty()) {
            return;
        }

        offering.getAssociatedClasses().stream()
                .filter(classInfo -> "COMPULSORY".equals(classInfo.getCourseType()))
                .forEach(compulsoryClass -> {
                    // 根据班级ID和学籍信息查找学生
                    List<Student> students = studentMapper.findByClassAndAcademicInfo(
                            compulsoryClass.getClassId(),
                            offering.getAcademicYear(),
                            offering.getSemester()
                    );

                    if (students.isEmpty()) {
                        log.info("班级 '{}' 在 {}-{}学年/{}学期 没有找到需要分配此必修课的学生。",
                                compulsoryClass.getClassName(), offering.getAcademicYear(), offering.getAcademicYear() + 1, offering.getSemester());
                        return;
                    }

                    students.forEach(student -> {
                        try {
                            this.enrollCourseForStudentInternal(offering.getId(), student.getId());
                        } catch (Exception e) {
                            log.warn("为学生(ID:{})自动分配课程(ID:{})时发生错误: {}", student.getId(), offering.getId(), e.getMessage());
                        }
                    });
                });
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enrollCourseInNewTransaction(Long offeringDbId, Long studentDbId) {
        this.enrollCourseForStudentInternal(offeringDbId, studentDbId);
    }
    /**
     * 为单个学生自动分配其学年和学期对应的必修课
     */
    @Transactional
    public void assignCompulsoryCoursesForStudent(Student student) {
        if (student == null || student.getClassId() == null || student.getAcademicYear() == null || student.getSemester() == null) {
            return;
        }
        // 【修正】调用新的 Mapper 方法
        List<CourseOffering> compulsoryOfferings = offeringClassLinkMapper.findCompulsoryOfferingsForClass(
                student.getClassId(), student.getAcademicYear(), student.getSemester()
        );
        if (compulsoryOfferings.isEmpty()) {
            return;
        }
        List<Enrollment> currentEnrollments = enrollmentMapper.findByStudentId(student.getId());
        Set<Long> enrolledOfferingIds = currentEnrollments.stream()
                .map(Enrollment::getCourseOfferingId)
                .collect(Collectors.toSet());
        for (CourseOffering offering : compulsoryOfferings) {
            if (!enrolledOfferingIds.contains(offering.getId())) {
                enrollCourseForStudentInternal(offering.getId(), student.getId());
            }
        }
    }

    /**
     * 学生为自己选课
     */
    @Transactional
    public Enrollment enrollCourseForStudent(Long offeringDbId, Long studentDbId) {
        if (!systemSettingService.isCourseSelectionOpen()) {
            throw new IllegalStateException("当前非选课时间，无法进行操作。");
        }
        CourseOffering targetOffering = courseOfferingMapper.findById(offeringDbId);
        if (targetOffering == null) {
            throw new ResourceNotFoundException("ID为 " + offeringDbId + " 的课程安排不存在。");
        }
        Student student = studentMapper.findById(studentDbId);
        // 【修正】判断是否为学生所在班级的必修课
        boolean isCompulsoryForStudent = targetOffering.getAssociatedClasses().stream()
                .anyMatch(classInfo -> classInfo.getClassId().equals(student.getClassId()) && "COMPULSORY".equals(classInfo.getCourseType()));
        if (isCompulsoryForStudent) {
            throw new IllegalArgumentException("【" + targetOffering.getCourseName() + "】是您的班级必修课，由系统自动分配，无需手动选择。");
        }
        return enrollCourseForStudentInternal(offeringDbId, studentDbId);
    }

    /**
     * 学生退课
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
        CourseOffering offering = courseOfferingMapper.findById(enrollment.getCourseOfferingId());
        if (offering == null) {
            throw new ResourceNotFoundException("该选课记录关联的课程不存在。");
        }
        Student student = studentMapper.findById(studentId);
        // 【修正】判断是否为学生所在班级的必修课
        boolean isCompulsoryForStudent = offering.getAssociatedClasses().stream()
                .anyMatch(classInfo -> classInfo.getClassId().equals(student.getClassId()) && "COMPULSORY".equals(classInfo.getCourseType()));
        if (isCompulsoryForStudent) {
            throw new IllegalStateException("【" + offering.getCourseName() + "】是您的班级必修课，无法退选。");
        }
        if (enrollment.getScore() != null) {
            throw new IllegalStateException("该课程已有成绩，无法退选。");
        }
        enrollmentMapper.deleteById(enrollmentId);
    }

    /**
     * 教师获取其课程安排的学生名单
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getEnrollmentsByOfferingForTeacher(Long offeringId, Long teacherId) {
        CourseOffering offering = courseOfferingMapper.findById(offeringId);
        if (offering == null || !offering.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("您无权访问该课程的信息。");
        }
        List<Enrollment> enrollments = enrollmentMapper.findByCourseOfferingId(offeringId);
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> studentIds = enrollments.stream().map(Enrollment::getStudentId).collect(Collectors.toList());
        List<Student> students = studentMapper.findByIds(studentIds);
        Map<Long, Student> studentMap = students.stream().collect(Collectors.toMap(Student::getId, Function.identity()));
        return enrollments.stream().map(enrollment -> {
            EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
            dto.setEnrollmentId(enrollment.getId());
            dto.setScore(enrollment.getScore());
            Student student = studentMap.get(enrollment.getStudentId());
            if (student != null) {
                dto.setStudentName(student.getName());
                dto.setStudentId(student.getStudentId());
            }
            dto.setCourseName(offering.getCourseName());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 教师更新成绩
     */
    @Transactional
    public void updateGrade(Long enrollmentId, Double score, Long teacherId) {
        Enrollment enrollment = enrollmentMapper.findById(enrollmentId);
        if (enrollment == null) {
            throw new ResourceNotFoundException("该选课记录不存在。");
        }
        CourseOffering offering = courseOfferingMapper.findById(enrollment.getCourseOfferingId());
        if (offering == null || !offering.getTeacherId().equals(teacherId)) {
            throw new AccessDeniedException("您无权修改该课程的成绩。");
        }
        enrollmentMapper.updateScore(enrollmentId, score);
    }

    /**
     * 学生获取自己的所有选课记录
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getEnrollmentsForStudent(Long studentId) {
        List<Enrollment> enrollments = enrollmentMapper.findByStudentId(studentId);
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }
        return enrollments.stream().map(enrollment -> {
            EnrollmentResponseDTO dto = new EnrollmentResponseDTO();
            dto.setEnrollmentId(enrollment.getId());
            dto.setScore(enrollment.getScore());

            // 【核心新增】为DTO设置 courseOfferingId
            dto.setCourseOfferingId(enrollment.getCourseOfferingId());

            CourseOffering offering = courseOfferingMapper.findById(enrollment.getCourseOfferingId());
            if (offering != null) {
                dto.setCourseName(offering.getCourseName());
                dto.setCourseId(offering.getCourseCode());
                dto.setCredits(offering.getCredits());
                dto.setCourseDay(offering.getCourseDay());
                dto.setCourseTime(offering.getCourseTime());
                dto.setTeacherName(offering.getTeacherName() != null ? offering.getTeacherName() : "未知教师");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取学生可选的课程安排
     */
    @Transactional(readOnly = true)
    public List<CourseOffering> getAvailableOfferingsForStudent(Long studentId) {
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            return Collections.emptyList();
        }
        List<CourseOffering> allOfferings = courseOfferingMapper.findAllWithDetails();
        if (allOfferings.isEmpty()) {
            return Collections.emptyList();
        }
        return allOfferings.stream()
                .filter(offering ->
                        offering.getAcademicYear().equals(student.getAcademicYear()) &&
                                offering.getSemester().equals(student.getSemester()))
                .collect(Collectors.toList());
    }

    /**
     * 管理员创建选课记录
     */
    @Transactional
    public Enrollment createEnrollment(EnrollmentDTO enrollmentDTO) {
        Student student = studentMapper.findByStudentId(enrollmentDTO.getStudentId());
        if (student == null) {
            throw new ResourceNotFoundException("学号为 " + enrollmentDTO.getStudentId() + " 的学生不存在。");
        }
        long offeringId;
        try {
            // 注意：DTO中的courseId被用作offeringId
            offeringId = Long.parseLong(enrollmentDTO.getCourseId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的课程安排ID格式。");
        }
        CourseOffering offering = courseOfferingMapper.findById(offeringId);
        if (offering == null) {
            throw new ResourceNotFoundException("ID为 " + offeringId + " 的课程安排不存在。");
        }
        return enrollCourseForStudentInternal(offering.getId(), student.getId());
    }

    /**
     * 内部核心选课逻辑
     */
    public Enrollment enrollCourseForStudentInternal(Long offeringDbId, Long studentDbId) {
        List<Enrollment> enrollments = enrollmentMapper.findByStudentId(studentDbId);
        if (!CollectionUtils.isEmpty(enrollments)) {
            boolean alreadyEnrolled = enrollments.stream().anyMatch(e -> e.getCourseOfferingId().equals(offeringDbId));
            if (alreadyEnrolled) {
                // ✨ 修改：为了兼容自动分配课程的场景，此处不再抛出异常，而是静默返回。
                log.warn("学生(ID:{})已选修课程(Offering ID:{})，跳过此次注册。", studentDbId, offeringDbId);
                return null;
            }
            CourseOffering targetOffering = courseOfferingMapper.findById(offeringDbId);
            List<Long> enrolledOfferingIds = enrollments.stream().map(Enrollment::getCourseOfferingId).collect(Collectors.toList());
            List<CourseOffering> enrolledOfferings = enrolledOfferingIds.stream()
                    .map(courseOfferingMapper::findById)
                    .filter(Objects::nonNull).collect(Collectors.toList());
            for (CourseOffering enrolledOffering : enrolledOfferings) {
                if (enrolledOffering.getCourseDay() != null && enrolledOffering.getCourseTime() != null &&
                        targetOffering.getCourseDay() != null && targetOffering.getCourseTime() != null &&
                        Objects.equals(enrolledOffering.getCourseDay(), targetOffering.getCourseDay()) &&
                        Objects.equals(enrolledOffering.getCourseTime(), targetOffering.getCourseTime())) {
                    throw new IllegalArgumentException("选课失败：与已选课程 '" + enrolledOffering.getCourseName() + "' 时间冲突。");
                }
            }
        }
        Enrollment newEnrollment = new Enrollment();
        newEnrollment.setStudentId(studentDbId);
        newEnrollment.setCourseOfferingId(offeringDbId);
        try {
            enrollmentMapper.insert(newEnrollment);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("数据库唯一性约束冲突，可能已选修此课程。");
        }
        return newEnrollment;
    }
}