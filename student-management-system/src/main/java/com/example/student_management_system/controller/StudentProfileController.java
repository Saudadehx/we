package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.StudentProfileUpdateDTO;
import com.example.student_management_system.dto.StudentResponseDTO;
import com.example.student_management_system.model.Student;
import com.example.student_management_system.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-profile") // 告诉系统，这个类负责处理所有 /api/my-profile 的请求
public class StudentProfileController {

    @Autowired
    private StudentService studentService;

    // 处理 GET /api/my-profile 请求
    @GetMapping
    public ApiResult<StudentResponseDTO> getMyProfile(@AuthenticationPrincipal Student student) {
        // Spring Security会把当前登录的Student对象神奇地“注入”进来
        return ApiResult.success(studentService.getStudentById(student.getId())
                .orElseThrow(() -> new RuntimeException("获取学生信息失败，ID: " + student.getId())));
    }

    // 处理 PUT /api/my-profile 请求
    @PutMapping
    public ApiResult<StudentResponseDTO> updateMyProfile(
            @AuthenticationPrincipal Student student,
            @RequestBody StudentProfileUpdateDTO profileDetails) {
        // 调用我们之前在Service层准备好的，专门给学生更新自己信息的方法
        return ApiResult.success(studentService.updateStudentProfile(student.getId(), profileDetails));
    }
}