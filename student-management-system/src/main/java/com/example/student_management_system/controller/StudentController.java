package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.DashboardStatsDTO;
import com.example.student_management_system.dto.StudentRequestDTO; // 1. 导入新的 DTO
import com.example.student_management_system.dto.StudentResponseDTO;
import com.example.student_management_system.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/stats")
    public ApiResult<DashboardStatsDTO> getStats() {
        return ApiResult.success(studentService.getDashboardStats());
    }

    @GetMapping
    public ApiResult<List<StudentResponseDTO>> getAllStudents() {
        return ApiResult.success(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ApiResult<StudentResponseDTO> getStudentById(@PathVariable Long id) {
        Optional<StudentResponseDTO> studentDto = studentService.getStudentById(id);
        return ApiResult.success(studentDto.orElseThrow(() -> new com.example.student_management_system.service.ResourceNotFoundException("未找到ID为 " + id + " 的学生。")));
    }

    @PostMapping
    // 2. 将 createStudent 的参数类型改为 StudentRequestDTO
    public ApiResult<StudentResponseDTO> createStudent(@Valid @RequestBody StudentRequestDTO studentDto) {
        StudentResponseDTO createdStudent = studentService.createStudent(studentDto);
        return ApiResult.success(createdStudent);
    }

    @PutMapping("/{id}")
    // 3. 将 updateStudent 的参数类型改为 StudentRequestDTO
    public ApiResult<StudentResponseDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequestDTO studentDetails) {
        StudentResponseDTO updatedStudent = studentService.updateStudent(id, studentDetails);
        return ApiResult.success(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ApiResult<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ApiResult.success();
    }
}