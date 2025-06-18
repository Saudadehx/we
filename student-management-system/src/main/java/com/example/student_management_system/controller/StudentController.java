package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.DashboardStatsDTO;
import com.example.student_management_system.dto.StudentDTO; // ✨ 1. 导入新的 DTO
import com.example.student_management_system.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // ✨ 导入 PreAuthorize
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasRole('ADMIN')") // ✨ 2. 为整个控制器添加权限要求，所有接口默认需要ADMIN角色
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
    // ✨ 3. 修改返回类型
    public ApiResult<List<StudentDTO>> getAllStudents() {
        return ApiResult.success(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    // ✨ 4. 修改返回类型
    public ApiResult<StudentDTO> getStudentById(@PathVariable Long id) {
        Optional<StudentDTO> studentDto = studentService.getStudentById(id);
        return ApiResult.success(studentDto.orElseThrow(() -> new com.example.student_management_system.service.ResourceNotFoundException("未找到ID为 " + id + " 的学生。")));
    }

    @PostMapping
    // ✨ 5. 修改参数和返回类型
    public ApiResult<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDto) {
        StudentDTO createdStudent = studentService.createStudent(studentDto);
        return ApiResult.success(createdStudent);
    }

    @PutMapping("/{id}")
    // ✨ 6. 修改参数和返回类型
    public ApiResult<StudentDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentDTO studentDetails) {
        StudentDTO updatedStudent = studentService.updateStudent(id, studentDetails);
        return ApiResult.success(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ApiResult<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ApiResult.success();
    }
}