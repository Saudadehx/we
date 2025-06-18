package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.StudentDTO; // ✨ 1. 导入新的 DTO
import com.example.student_management_system.model.Student;
import com.example.student_management_system.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // ✨ 导入 PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-profile")
@PreAuthorize("hasRole('STUDENT')") // ✨ 2. 为整个控制器添加权限要求，确保只有学生能访问
public class StudentProfileController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    // ✨ 3. 修改返回类型
    public ApiResult<StudentDTO> getMyProfile(@AuthenticationPrincipal Student student) {
        return ApiResult.success(studentService.getStudentById(student.getId())
                .orElseThrow(() -> new RuntimeException("获取学生信息失败，ID: " + student.getId())));
    }

    @PutMapping
    // ✨ 4. 修改参数和返回类型
    public ApiResult<StudentDTO> updateMyProfile(
            @AuthenticationPrincipal Student student,
            @RequestBody StudentDTO profileDetails) {
        return ApiResult.success(studentService.updateStudentProfile(student.getId(), profileDetails));
    }
}