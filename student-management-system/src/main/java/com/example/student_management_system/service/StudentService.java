package com.example.student_management_system.service;

import com.example.student_management_system.dto.DashboardStatsDTO;
import com.example.student_management_system.dto.StudentDTO;
import com.example.student_management_system.mapper.MajorMapper;
import com.example.student_management_system.mapper.StudentMapper;
import com.example.student_management_system.model.Major;
import com.example.student_management_system.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final MajorMapper majorMapper;
    private final EnrollmentService enrollmentService;

    @Autowired
    public StudentService(StudentMapper studentMapper, PasswordEncoder passwordEncoder, MajorMapper majorMapper, EnrollmentService enrollmentService) {
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
        this.majorMapper = majorMapper;
        this.enrollmentService = enrollmentService;
    }

    // --- 数据转换辅助方法 ---

    /**
     * 将 Student 实体转换为 StudentDTO。
     */
    private StudentDTO convertToDto(Student student) {
        if (student == null) return null;

        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setStudentId(student.getStudentId());
        dto.setName(student.getName());
        dto.setGender(student.getGender());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setEthnicity(student.getEthnicity());
        dto.setNativePlace(student.getNativePlace());
        dto.setPoliticalStatus(student.getPoliticalStatus());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setEmail(student.getEmail());
        dto.setCollege(student.getCollege());
        dto.setClassName(student.getClassName());
        dto.setEnrollmentDate(student.getEnrollmentDate());
        dto.setStudentStatus(student.getStudentStatus());
        dto.setGpa(student.getGpa());
        dto.setPhotoUrl(student.getPhotoUrl());
        dto.setMajorId(student.getMajorId());
        dto.setAcademicYear(student.getAcademicYear());
        dto.setSemester(student.getSemester());

        if (student.getMajorId() != null) {
            Major major = majorMapper.findById(student.getMajorId());
            dto.setMajorName(major != null ? major.getName() : "未分配");
        } else {
            dto.setMajorName("未分配");
        }

        return dto;
    }

    /**
     * 【新增】一个辅助方法，用于从DTO更新实体，减少重复代码。
     * @param student 要被更新的实体对象
     * @param dto 包含新数据的DTO对象
     */
    private void updateEntityFromDto(Student student, StudentDTO dto) {
        student.setName(dto.getName());
        student.setGender(dto.getGender());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setEthnicity(dto.getEthnicity());
        student.setNativePlace(dto.getNativePlace());
        student.setPoliticalStatus(dto.getPoliticalStatus());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setEmail(dto.getEmail());
        student.setStudentId(dto.getStudentId());
        student.setCollege(dto.getCollege());
        student.setClassName(dto.getClassName());
        student.setEnrollmentDate(dto.getEnrollmentDate());
        student.setStudentStatus(dto.getStudentStatus());
        student.setGpa(dto.getGpa());
        student.setPhotoUrl(dto.getPhotoUrl());
        student.setMajorId(dto.getMajorId());
        student.setAcademicYear(dto.getAcademicYear());
        student.setSemester(dto.getSemester());
    }


    // --- 公共服务方法 ---

    public DashboardStatsDTO getDashboardStats() {
        List<Student> students = studentMapper.findAll(Collections.emptyMap());
        List<Major> majors = majorMapper.findAll();

        long totalStudents = students.size();
        long totalClasses = students.stream().map(Student::getClassName).distinct().count();
        long totalMajors = majors.size();
        // 年级计算逻辑保持不变
        long totalGrades = students.stream()
                .map(s -> s.getClassName().replaceAll("[^0-9]", ""))
                .filter(s -> !s.isEmpty())
                .map(s -> s.substring(0, Math.min(s.length(), 4)))
                .distinct()
                .count();

        DashboardStatsDTO stats = new DashboardStatsDTO();
        stats.setTotalStudents(totalStudents);
        stats.setTotalClasses(totalClasses);
        stats.setTotalMajors(totalMajors);
        stats.setTotalGrades(totalGrades);
        return stats;
    }

    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentMapper.findAll(Collections.emptyMap()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<StudentDTO> getStudentById(Long id) {
        Student student = studentMapper.findById(id);
        return Optional.ofNullable(convertToDto(student));
    }

    /**
     * 【优化】创建学生的方法，逻辑更清晰
     */
    @Transactional
    public StudentDTO createStudent(StudentDTO studentDto) {
        // 1. 校验学号是否存在
        if (studentMapper.findByStudentId(studentDto.getStudentId()) != null) {
            throw new IllegalArgumentException("学号 " + studentDto.getStudentId() + " 已存在。");
        }
        // 2. 校验初始密码
        if (!StringUtils.hasText(studentDto.getPassword())) {
            throw new IllegalArgumentException("创建学生时，初始密码不能为空。");
        }

        // 3. 创建并填充实体
        Student student = new Student();
        updateEntityFromDto(student, studentDto); // 使用辅助方法填充
        student.setPassword(passwordEncoder.encode(studentDto.getPassword()));

        // 4. 插入数据库
        studentMapper.insert(student);
        Student createdStudent = studentMapper.findByStudentId(student.getStudentId());

        // 5. 分配必修课
        enrollmentService.assignCompulsoryCoursesForStudent(createdStudent);

        // 6. 返回DTO
        return convertToDto(createdStudent);
    }

    /**
     * 【优化】更新学生信息的方法，逻辑更清晰
     */
    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDetails) {
        // 1. 查找现有学生
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生。");
        }

        // 2. 记录旧的学籍信息，用于后续比较
        Long oldMajorId = student.getMajorId();
        Integer oldAcademicYear = student.getAcademicYear();
        Integer oldSemester = student.getSemester();

        // 3. 校验新学号是否冲突
        if (studentDetails.getStudentId() != null && !studentDetails.getStudentId().equals(student.getStudentId())) {
            if (studentMapper.findByStudentId(studentDetails.getStudentId()) != null) {
                throw new IllegalArgumentException("学号 " + studentDetails.getStudentId() + " 已被其他学生使用。");
            }
        }

        // 4. 从DTO更新实体信息
        updateEntityFromDto(student, studentDetails); // 使用辅助方法

        // 5. 如果提供了新密码，则加密并更新
        if (StringUtils.hasText(studentDetails.getPassword())) {
            student.setPassword(passwordEncoder.encode(studentDetails.getPassword()));
        }

        // 6. 更新数据库
        int affectedRows = studentMapper.update(student);
        if (affectedRows == 0) {
            throw new RuntimeException("更新学生信息失败，数据可能已被他人修改，请刷新后重试。");
        }

        // 7. 检查学籍信息是否变更，如果变更则同步课程注册
        boolean academicInfoChanged = !Objects.equals(oldMajorId, student.getMajorId()) ||
                !Objects.equals(oldAcademicYear, student.getAcademicYear()) ||
                !Objects.equals(oldSemester, student.getSemester());

        if (academicInfoChanged) {
            enrollmentService.reconcileEnrollmentsForStudent(student);
        }

        // 8. 返回更新后的DTO
        return convertToDto(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (studentMapper.findById(id) == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生，无法删除。");
        }
        // 注意：这里没有处理级联删除选课记录，取决于数据库设计。
        // 如果有外键约束，直接删除可能会失败。
        studentMapper.deleteById(id);
    }

    @Transactional
    public StudentDTO updateStudentProfile(Long studentId, StudentDTO profileDetails) {
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + studentId + " 的学生。");
        }

        // 学生只能更新部分信息
        if (StringUtils.hasText(profileDetails.getPassword())) {
            student.setPassword(passwordEncoder.encode(profileDetails.getPassword()));
        }
        student.setPhoneNumber(profileDetails.getPhoneNumber());
        student.setEmail(profileDetails.getEmail());
        student.setEthnicity(profileDetails.getEthnicity());
        student.setNativePlace(profileDetails.getNativePlace());
        student.setPoliticalStatus(profileDetails.getPoliticalStatus());

        int affectedRows = studentMapper.update(student);
        if (affectedRows == 0) {
            throw new RuntimeException("更新个人信息失败，请刷新后重试。");
        }

        return convertToDto(student);
    }
}