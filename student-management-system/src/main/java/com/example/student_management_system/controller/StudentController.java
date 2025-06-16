package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.StudentCreateDTO;
import com.example.student_management_system.dto.StudentResponseDTO;
import com.example.student_management_system.dto.StudentUpdateDTO;
import com.example.student_management_system.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.student_management_system.dto.DashboardStatsDTO;

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

    // ✨ 修改点1：所有返回类型都用 ApiResult<> 包装
    @GetMapping("/stats")
    public ApiResult<DashboardStatsDTO> getStats() {
        return ApiResult.success(studentService.getDashboardStats());
    }

    @GetMapping
    public ApiResult<List<StudentResponseDTO>> getAllStudents() {
        return ApiResult.success(studentService.getAllStudents());
    }

    // ✨ 修改点2：对于可能为空的结果，我们先判断再返回成功或失败
    @GetMapping("/{id}")
    public ApiResult<StudentResponseDTO> getStudentById(@PathVariable Long id) {
        Optional<StudentResponseDTO> studentDto = studentService.getStudentById(id);
        // 如果找到了学生，就返回成功的ApiResult；否则，让全局异常处理器处理
        // 注意：这里我们让 Service 抛出 ResourceNotFoundException，由全局处理器捕获
        // 为了保持一致性，我们改造一下Service，让它在找不到时抛出异常
        return ApiResult.success(studentDto.orElseThrow(() -> new com.example.student_management_system.service.ResourceNotFoundException("未找到ID为 " + id + " 的学生。")));
    }

    @PostMapping
    public ApiResult<StudentResponseDTO> createStudent(@Valid @RequestBody StudentCreateDTO studentDto) {
        StudentResponseDTO createdStudent = studentService.createStudent(studentDto);
        return ApiResult.success(createdStudent);
    }

    @PutMapping("/{id}")
    public ApiResult<StudentResponseDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateDTO studentDetails) {
        StudentResponseDTO updatedStudent = studentService.updateStudent(id, studentDetails);
        return ApiResult.success(updatedStudent);
    }

    @DeleteMapping("/{id}")
    public ApiResult<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        // 对于没有返回数据的操作，直接调用无参的 success()
        return ApiResult.success();
    }
}