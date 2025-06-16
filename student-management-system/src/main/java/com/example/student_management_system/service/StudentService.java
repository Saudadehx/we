package com.example.student_management_system.service;

import com.example.student_management_system.dto.*;
import com.example.student_management_system.mapper.StudentMapper;
import com.example.student_management_system.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public StudentService(StudentMapper studentMapper, PasswordEncoder passwordEncoder) {
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    private StudentResponseDTO convertToResponseDto(Student student) {
        if (student == null) return null;
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
        dto.setMajor(student.getMajor());
        dto.setEnrollmentDate(student.getEnrollmentDate());
        dto.setStudentStatus(student.getStudentStatus());
        dto.setGpa(student.getGpa());
        dto.setPhotoUrl(student.getPhotoUrl());
        return dto;
    }

    public DashboardStatsDTO getDashboardStats() {
        // ... 此方法不需要修改
        List<Student> students = studentMapper.findAll();
        long totalStudents = students.size();
        long totalClasses = students.stream().map(Student::getClassName).distinct().count();
        long totalMajors = students.stream().map(Student::getMajor).filter(m -> m != null && !m.isEmpty()).distinct().count();
        long totalGrades = students.stream()
                .map(s -> s.getClassName().replaceAll("[^0-9]", ""))
                .filter(s -> !s.isEmpty())
                .map(s -> s.substring(0, 2))
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
        return studentMapper.findAll().stream()
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

        // 【关键】确保 password 字段从 DTO 正确映射并加密
        if (StringUtils.hasText(studentDto.getPassword())) {
            student.setPassword(passwordEncoder.encode(studentDto.getPassword()));
        }

        // 确保所有其他字段都从 DTO 正确映射
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
        student.setMajor(studentDto.getMajor());
        student.setEnrollmentDate(studentDto.getEnrollmentDate());
        student.setStudentStatus(studentDto.getStudentStatus());
        student.setGpa(studentDto.getGpa());
        student.setPhotoUrl(studentDto.getPhotoUrl());

        studentMapper.insert(student);

        // 返回新创建的学生信息
        Student createdStudent = studentMapper.findByStudentId(student.getStudentId());
        return convertToResponseDto(createdStudent);
    }


    @Transactional
    public StudentResponseDTO updateStudent(Long id, StudentUpdateDTO studentDetails) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生。");
        }

        if (studentDetails.getStudentId() != null && !studentDetails.getStudentId().equals(student.getStudentId())) {
            Student studentWithNewStudentId = studentMapper.findByStudentId(studentDetails.getStudentId());
            if (studentWithNewStudentId != null) {
                throw new IllegalArgumentException("学号 " + studentDetails.getStudentId() + " 已被其他学生使用。");
            }
        }
        if (StringUtils.hasText(studentDetails.getPassword())) {
            student.setPassword(passwordEncoder.encode(studentDetails.getPassword()));
        }
        // 基本信息
        student.setName(studentDetails.getName());
        student.setGender(studentDetails.getGender());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setEthnicity(studentDetails.getEthnicity());
        student.setNativePlace(studentDetails.getNativePlace());
        student.setPoliticalStatus(studentDetails.getPoliticalStatus());

        // 联系方式
        student.setPhoneNumber(studentDetails.getPhoneNumber());
        student.setEmail(studentDetails.getEmail());

        // 学籍信息
        student.setStudentId(studentDetails.getStudentId());
        student.setCollege(studentDetails.getCollege());
        student.setClassName(studentDetails.getClassName());
        student.setMajor(studentDetails.getMajor());
        student.setEnrollmentDate(studentDetails.getEnrollmentDate());
        student.setStudentStatus(studentDetails.getStudentStatus());
        student.setGpa(studentDetails.getGpa());
        student.setPhotoUrl(studentDetails.getPhotoUrl());

        int affectedRows = studentMapper.update(student);
        if (affectedRows == 0) {
            // 如果影响行数为0，说明更新没有成功，可能是数据版本问题或ID不存在
            throw new RuntimeException("更新学生信息失败，数据可能已被他人修改，请刷新后重试。");
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

        // 学生只能更新部分信息
        if (StringUtils.hasText(profileDetails.getPassword())) {
            student.setPassword(passwordEncoder.encode(profileDetails.getPassword()));
        }
        student.setPhoneNumber(profileDetails.getPhoneNumber());
        student.setEmail(profileDetails.getEmail());

        // 【关键修改】同样检查update操作影响的行数
        int affectedRows = studentMapper.update(student);
        if (affectedRows == 0) {
            throw new RuntimeException("更新个人信息失败，请刷新后重试。");
        }

        return convertToResponseDto(student);
    }
}