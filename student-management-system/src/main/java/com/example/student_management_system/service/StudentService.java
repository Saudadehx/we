package com.example.student_management_system.service;

import com.example.student_management_system.dto.DashboardStatsDTO;
import com.example.student_management_system.dto.StudentDTO;
import com.example.student_management_system.mapper.MajorMapper;
import com.example.student_management_system.mapper.StudentMapper;
import com.example.student_management_system.model.Major;
import com.example.student_management_system.model.Student;
import org.springframework.beans.BeanUtils;
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

    private StudentDTO convertToDto(Student student) {
        if (student == null) return null;

        StudentDTO dto = new StudentDTO();
        BeanUtils.copyProperties(student, dto);

        if (student.getMajorId() != null) {
            Major major = majorMapper.findById(student.getMajorId());
            dto.setMajorName(major != null ? major.getName() : "未分配");
        } else {
            dto.setMajorName("未分配");
        }

        return dto;
    }

    private void updateEntityFromDto(Student student, StudentDTO dto) {
        BeanUtils.copyProperties(dto, student, "id", "password", "majorName");
    }


    // --- 公共服务方法 ---

    public DashboardStatsDTO getDashboardStats() {
        List<Student> students = studentMapper.findAll(Collections.emptyMap());
        List<Major> majors = majorMapper.findAll();

        long totalStudents = students.size();
        long totalClasses = students.stream().map(Student::getClassName).distinct().count();
        long totalMajors = majors.size();
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

    @Transactional
    public StudentDTO createStudent(StudentDTO studentDto) {
        if (studentMapper.findByStudentId(studentDto.getStudentId()) != null) {
            throw new IllegalArgumentException("学号 " + studentDto.getStudentId() + " 已存在。");
        }
        if (!StringUtils.hasText(studentDto.getPassword())) {
            throw new IllegalArgumentException("创建学生时，初始密码不能为空。");
        }

        Student student = new Student();
        updateEntityFromDto(student, studentDto);
        student.setPassword(passwordEncoder.encode(studentDto.getPassword()));

        studentMapper.insert(student);
        Student createdStudent = studentMapper.findByStudentId(student.getStudentId());

        enrollmentService.assignCompulsoryCoursesForStudent(createdStudent);

        return convertToDto(createdStudent);
    }

    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDetails) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生。");
        }

        Long oldMajorId = student.getMajorId();
        Integer oldAcademicYear = student.getAcademicYear();
        Integer oldSemester = student.getSemester();

        if (studentDetails.getStudentId() != null && !studentDetails.getStudentId().equals(student.getStudentId())) {
            if (studentMapper.findByStudentId(studentDetails.getStudentId()) != null) {
                throw new IllegalArgumentException("学号 " + studentDetails.getStudentId() + " 已被其他学生使用。");
            }
        }

        updateEntityFromDto(student, studentDetails);

        if (StringUtils.hasText(studentDetails.getPassword())) {
            student.setPassword(passwordEncoder.encode(studentDetails.getPassword()));
        }

        int affectedRows = studentMapper.update(student);
        if (affectedRows == 0) {
            throw new RuntimeException("更新学生信息失败，数据可能已被他人修改，请刷新后重试。");
        }

        boolean academicInfoChanged = !Objects.equals(oldMajorId, student.getMajorId()) ||
                !Objects.equals(oldAcademicYear, student.getAcademicYear()) ||
                !Objects.equals(oldSemester, student.getSemester());

        if (academicInfoChanged) {
            enrollmentService.reconcileEnrollmentsForStudent(student);
        }

        return convertToDto(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        if (studentMapper.findById(id) == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生，无法删除。");
        }
        studentMapper.deleteById(id);
    }

    @Transactional
    public StudentDTO updateStudentProfile(Long studentId, StudentDTO profileDetails) {
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + studentId + " 的学生。");
        }

        // ✨【修正】恢复到更安全的“白名单”模式，只更新允许学生修改的字段。
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