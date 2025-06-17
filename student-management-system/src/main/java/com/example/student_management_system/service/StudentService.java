package com.example.student_management_system.service;

import com.example.student_management_system.dto.*;
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
    private final EnrollmentService enrollmentService; // 注入EnrollmentService

    @Autowired
    public StudentService(StudentMapper studentMapper, PasswordEncoder passwordEncoder, MajorMapper majorMapper, EnrollmentService enrollmentService) {
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
        this.majorMapper = majorMapper;
        this.enrollmentService = enrollmentService; // 初始化
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
        // 【修改】直接从 majorMapper 获取所有专业来进行统计
        List<Student> students = studentMapper.findAll(Collections.emptyMap());
        List<Major> majors = majorMapper.findAll();

        long totalStudents = students.size();
        long totalClasses = students.stream().map(Student::getClassName).distinct().count();
        long totalMajors = majors.size(); // 直接获取专业总数
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
    public StudentResponseDTO createStudent(StudentCreateDTO studentDto) {
        Student existingStudent = studentMapper.findByStudentId(studentDto.getStudentId());
        if (existingStudent != null) {
            throw new IllegalArgumentException("学号 " + studentDto.getStudentId() + " 已存在。");
        }

        Student student = new Student();

        if (StringUtils.hasText(studentDto.getPassword())) {
            student.setPassword(passwordEncoder.encode(studentDto.getPassword()));
        } else {
            // 如果密码为空，可以设置一个默认密码，或抛出异常
            throw new IllegalArgumentException("初始密码不能为空。");
        }

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
        // 插入后，student对象还没有数据库ID，需要重新查询一次
        Student createdStudent = studentMapper.findByStudentId(student.getStudentId());

        // 【核心修改】为新创建的学生自动分配必修课
        enrollmentService.assignCompulsoryCoursesForStudent(createdStudent);

        return convertToResponseDto(createdStudent);
    }


    @Transactional
    public StudentResponseDTO updateStudent(Long id, StudentUpdateDTO studentDetails) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生。");
        }
        // 记录旧的学籍信息，用于判断是否需要重新分配课程
        Long oldMajorId = student.getMajorId();
        Integer oldAcademicYear = student.getAcademicYear();
        Integer oldSemester = student.getSemester();

        if (studentDetails.getStudentId() != null && !studentDetails.getStudentId().equals(student.getStudentId())) {
            Student studentWithNewStudentId = studentMapper.findByStudentId(studentDetails.getStudentId());
            if (studentWithNewStudentId != null) {
                throw new IllegalArgumentException("学号 " + studentDetails.getStudentId() + " 已被其他学生使用。");
            }
        }
        if (StringUtils.hasText(studentDetails.getPassword())) {
            student.setPassword(passwordEncoder.encode(studentDetails.getPassword()));
        }

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

        // 【核心修改】检查学籍信息是否有变，如有则重新分配课程
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
    public StudentResponseDTO updateStudentProfile(Long studentId, StudentProfileUpdateDTO profileDetails) {
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + studentId + " 的学生。");
        }

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