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
@RequestMapping("/api/students") // 修改路径为 /api/students
@PreAuthorize("hasRole('ADMIN')") // 为整个控制器添加权限要求，所有接口默认需要ADMIN角色
public class StudentController {
    // 学生控制器，处理学生相关的请求
    private final StudentService studentService;
    // 通过构造函数注入学生服务
    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }
    // 获取仪表盘统计信息
    @GetMapping("/stats")
    public ApiResult<DashboardStatsDTO> getStats() {
        return ApiResult.success(studentService.getDashboardStats());
    }
    // 获取所有学生信息
    @GetMapping
    // 修改返回类型
    public ApiResult<List<StudentDTO>> getAllStudents() {
        return ApiResult.success(studentService.getAllStudents());
    }
    // 根据ID获取学生信息
    @GetMapping("/{id}")
    // 修改返回类型
    public ApiResult<StudentDTO> getStudentById(@PathVariable Long id) {
        Optional<StudentDTO> studentDto = studentService.getStudentById(id);
        return ApiResult.success(studentDto.orElseThrow(() -> new com.example.student_management_system.service.ResourceNotFoundException("未找到ID为 " + id + " 的学生。")));
    }
    // 创建新学生
    @PostMapping
    // 修改参数和返回类型
    public ApiResult<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDto) { // 接收学生信息并创建新学生
        StudentDTO createdStudent = studentService.createStudent(studentDto); // 调用服务层创建新学生
        return ApiResult.success(createdStudent); // 返回创建的学生信息
    }
    // 更新学生信息
    @PutMapping("/{id}")
    // 修改参数和返回类型
    public ApiResult<StudentDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentDTO studentDetails) { // 更新学生信息
        StudentDTO updatedStudent = studentService.updateStudent(id, studentDetails);  // 调用服务层更新学生信息
        return ApiResult.success(updatedStudent); // 返回更新后的学生信息
    }
    // 删除学生信息
    @DeleteMapping("/{id}")
    public ApiResult<?> deleteStudent(@PathVariable Long id) { // 删除学生信息
        studentService.deleteStudent(id); // 调用服务层删除学生
        return ApiResult.success(); // 返回成功响应
    }
}