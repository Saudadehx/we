package com.example.student_management_system.service;

import com.example.student_management_system.dto.DashboardStatsDTO;
import com.example.student_management_system.dto.StudentCreateDTO;
import com.example.student_management_system.dto.StudentResponseDTO;
import com.example.student_management_system.dto.StudentUpdateDTO;
import com.example.student_management_system.mapper.StudentMapper;
import com.example.student_management_system.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentMapper studentMapper;

    @Autowired
    public StudentService(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    // ✨ 转换方法：将 Student 实体转换为 StudentResponseDTO
    private StudentResponseDTO convertToResponseDto(Student student) {
        if (student == null) return null;
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setId(student.getId());
        dto.setStudentId(student.getStudentId());
        dto.setName(student.getName());
        dto.setGender(student.getGender());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setClassName(student.getClassName());
        dto.setMajor(student.getMajor());
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

    // ✨ 修改点1：返回值类型从 List<Student> 改为 List<StudentResponseDTO>
    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAllStudents() {
        return studentMapper.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // ✨ 修改点2：返回值类型从 Optional<Student> 改为 Optional<StudentResponseDTO>
    @Transactional(readOnly = true)
    public Optional<StudentResponseDTO> getStudentById(Long id) {
        Student student = studentMapper.findById(id);
        return Optional.ofNullable(convertToResponseDto(student));
    }

    // ✨ 修改点3：参数类型从 Student 改为 StudentCreateDTO，返回值改为 StudentResponseDTO
    @Transactional
    public StudentResponseDTO createStudent(StudentCreateDTO studentDto) {
        Student existingStudent = studentMapper.findByStudentId(studentDto.getStudentId());
        if (existingStudent != null) {
            throw new IllegalArgumentException("学号 " + studentDto.getStudentId() + " 已存在。");
        }

        // 将 DTO 转换为实体
        Student student = new Student();
        student.setStudentId(studentDto.getStudentId());
        student.setName(studentDto.getName());
        student.setGender(studentDto.getGender());
        student.setDateOfBirth(studentDto.getDateOfBirth());
        student.setClassName(studentDto.getClassName());
        student.setMajor(studentDto.getMajor());
        student.setGpa(studentDto.getGpa());
        student.setPhotoUrl(studentDto.getPhotoUrl());

        studentMapper.insert(student);

        // 将新创建的、带有ID的实体转换回 DTO 返回给 Controller
        return convertToResponseDto(studentMapper.findByStudentId(student.getStudentId()));
    }

    // ✨ 修改点4：参数类型从 Student 改为 StudentUpdateDTO，返回值改为 StudentResponseDTO
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

        // 使用 DTO 的数据更新实体
        student.setStudentId(studentDetails.getStudentId());
        student.setName(studentDetails.getName());
        student.setGender(studentDetails.getGender());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setClassName(studentDetails.getClassName());
        student.setMajor(studentDetails.getMajor());
        student.setGpa(studentDetails.getGpa());
        student.setPhotoUrl(studentDetails.getPhotoUrl());

        studentMapper.update(student);

        // 返回更新后的 DTO
        return convertToResponseDto(student);
    }

    // ✨ 修改点5：删除方法无需修改，因为它只处理ID
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentMapper.findById(id);
        if (student == null) {
            throw new ResourceNotFoundException("未找到ID为 " + id + " 的学生，无法删除。");
        }
        studentMapper.deleteById(id);
    }
}