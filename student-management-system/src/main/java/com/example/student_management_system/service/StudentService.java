package com.example.student_management_system.service;

import com.example.student_management_system.dto.DashboardStatsDTO;
import com.example.student_management_system.dto.StudentRequestDTO; // 1. 导入新的 DTO
import com.example.student_management_system.dto.StudentResponseDTO;
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

    private StudentResponseDTO convertToResponseDto(Student student) {
        if (student == null) return null;
        Major major = null;
        if (student.getMajorId() != null) {
            major = majorMapper.findById(student.getMajorId());
        }

        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setGender(student.getGender());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setEthnicity(student.getEthnicity());
        dto.setNativePlace(student.getNativePlace());
        dto.setPoliticalStatus(student.getPoliticalStatus());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setEmail(student.getEmail());
        dto.setStudentId(student.getStudentId());
        dto.setCollege(student.getCollege());
        dto.setClassName(student.getClassName());
        dto.setEnrollmentDate(student.getEnrollmentDate());
        dto.setStudentStatus(student.getStudentStatus());
        dto.setGpa(student.getGpa());
        dto.setPhotoUrl(student.getPhotoUrl());
        dto.setMajorId(student.getMajorId());
        dto.setAcademicYear(student.getAcademicYear());
        dto.setSemester(student.getSemester());
        dto.setMajorName(major != null ? major.getName() : "未分配");

        return dto;
    }

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
    public List<StudentResponseDTO> getAllStudents() {
        return studentMapper.findAll(Collections.emptyMap()).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Optional<StudentResponseDTO> getStudentById(Long id) {
        Student student = studentMapper.findById(id);
        return Optional.ofNullable(convertToResponseDto(student));
    }


    @Transactional
    // 2. 修改 createStudent 方法签名
    public StudentResponseDTO createStudent(StudentRequestDTO studentDto) {
        Student existingStudent = studentMapper.findByStudentId(studentDto.getStudentId());
        if (existingStudent != null) {
            throw new IllegalArgumentException("学号 " + studentDto.getStudentId() + " 已存在。");
        }

        Student student = new Student();

        if (!StringUtils.hasText(studentDto.getPassword())) {
            throw new IllegalArgumentException("创建学生时，初始密码不能为空。");
        }
        student.setPassword(passwordEncoder.encode(studentDto.getPassword()));

        // 使用 DTO 中的数据填充实体
        student.setName(studentDto.getName());
        student.setGender(studentDto.getGender());
        student.setDateOfBirth(studentDto.getDateOfBirth());
        student.setEthnicity(studentDto.getEthnicity());
        student.setNativePlace(studentDto.getNativePlace());
        student.setPoliticalStatus(studentDto.getPoliticalStatus());
        student.setPhoneNumber(studentDto.getPhoneNumber());
        student.setEmail(studentDto.getEmail());
        student.setStudentId(studentDto.getStudentId());
        student.setCollege(studentDto.getCollege());
        student.setClassName(studentDto.getClassName());
        student.setEnrollmentDate(studentDto.getEnrollmentDate());
        student.setStudentStatus(studentDto.getStudentStatus());
        student.setGpa(studentDto.getGpa());
        student.setPhotoUrl(studentDto.getPhotoUrl());
        student.setMajorId(studentDto.getMajorId());
        student.setAcademicYear(studentDto.getAcademicYear());
        student.setSemester(studentDto.getSemester());

        studentMapper.insert(student);
        Student createdStudent = studentMapper.findByStudentId(student.getStudentId());
        enrollmentService.assignCompulsoryCoursesForStudent(createdStudent);
        return convertToResponseDto(createdStudent);
    }


    @Transactional
    // 3. 修改 updateStudent 方法签名
    public StudentResponseDTO updateStudent(Long id, StudentRequestDTO studentDetails) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生。");
        }
        Long oldMajorId = student.getMajorId();
        Integer oldAcademicYear = student.getAcademicYear();
        Integer oldSemester = student.getSemester();

        if (studentDetails.getStudentId() != null && !studentDetails.getStudentId().equals(student.getStudentId())) {
            Student studentWithNewStudentId = studentMapper.findByStudentId(studentDetails.getStudentId());
            if (studentWithNewStudentId != null) {
                throw new IllegalArgumentException("学号 " + studentDetails.getStudentId() + " 已被其他学生使用。");
            }
        }
        // 管理员更新时，如果密码字段不为空，则更新密码
        if (StringUtils.hasText(studentDetails.getPassword())) {
            student.setPassword(passwordEncoder.encode(studentDetails.getPassword()));
        }

        // 管理员可以更新所有字段
        student.setName(studentDetails.getName());
        student.setGender(studentDetails.getGender());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setEthnicity(studentDetails.getEthnicity());
        student.setNativePlace(studentDetails.getNativePlace());
        student.setPoliticalStatus(studentDetails.getPoliticalStatus());
        student.setPhoneNumber(studentDetails.getPhoneNumber());
        student.setEmail(studentDetails.getEmail());
        student.setStudentId(studentDetails.getStudentId());
        student.setCollege(studentDetails.getCollege());
        student.setClassName(studentDetails.getClassName());
        student.setEnrollmentDate(studentDetails.getEnrollmentDate());
        student.setStudentStatus(studentDetails.getStudentStatus());
        student.setGpa(studentDetails.getGpa());
        student.setPhotoUrl(studentDetails.getPhotoUrl());
        student.setMajorId(studentDetails.getMajorId());
        student.setAcademicYear(studentDetails.getAcademicYear());
        student.setSemester(studentDetails.getSemester());

        int affectedRows = studentMapper.update(student);
        if (affectedRows == 0) {
            throw new RuntimeException("更新学生信息失败，数据可能已被他人修改，请刷新后重试。");
        }

        boolean academicInfoChanged = !Objects.equals(oldMajorId, student.getMajorId()) ||
                !Objects.equals(oldAcademicYear, student.getAcademicYear()) ||
                !Objects.equals(oldSemester, student.getSemester());

        if (academicInfoChanged) {
            enrollmentService.assignCompulsoryCoursesForStudent(student);
        }

        return convertToResponseDto(student);
    }


    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生，无法删除。");
        }
        studentMapper.deleteById(id);
    }

    @Transactional
    // 4. 新增 updateStudentProfile 方法，并使其接收 StudentRequestDTO
    public StudentResponseDTO updateStudentProfile(Long studentId, StudentRequestDTO profileDetails) {
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + studentId + " 的学生。");
        }

        // 学生更新自己的信息时，只允许修改特定字段
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

        return convertToResponseDto(student);
    }
}