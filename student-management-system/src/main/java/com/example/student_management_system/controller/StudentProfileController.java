package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.StudentRequestDTO; // 1. 导入新的 DTO
import com.example.student_management_system.dto.StudentResponseDTO;
import com.example.student_management_system.model.Student;
import com.example.student_management_system.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-profile")
public class StudentProfileController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public ApiResult<StudentResponseDTO> getMyProfile(@AuthenticationPrincipal Student student) {
        return ApiResult.success(studentService.getStudentById(student.getId())
                .orElseThrow(() -> new RuntimeException("获取学生信息失败，ID: " + student.getId())));
    }

    @PutMapping
    // 2. 将 updateMyProfile 的参数类型改为 StudentRequestDTO
    public ApiResult<StudentResponseDTO> updateMyProfile(
            @AuthenticationPrincipal Student student,
            @RequestBody StudentRequestDTO profileDetails) {
        // 调用我们将在Service层改造的、专门给学生更新自己信息的方法
        return ApiResult.success(studentService.updateStudentProfile(student.getId(), profileDetails));
    }
}