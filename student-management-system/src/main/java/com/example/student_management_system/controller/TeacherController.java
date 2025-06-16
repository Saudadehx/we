package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.CourseResponseDTO;
import com.example.student_management_system.dto.TeacherDTO;
import com.example.student_management_system.model.Teacher;
import com.example.student_management_system.service.CourseService;
import com.example.student_management_system.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    // 注入用于处理教师信息的服务
    @Autowired
    private TeacherService teacherService;

    // 【关键修改】注入用于处理课程信息的服务
    @Autowired
    private CourseService courseService;

    // --- 以下是供管理员使用的接口 ---

    /**
     * 获取所有教师列表
     * @return 教师列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<List<TeacherDTO>> getAllTeachers() {
        return ApiResult.success(teacherService.getAllTeachers());
    }

    /**
     * 创建一个新教师
     * @param teacherDTO 包含教师工号、姓名和密码
     * @return 创建后的教师信息
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<TeacherDTO> createTeacher(@RequestBody TeacherDTO teacherDTO) {
        return ApiResult.success(teacherService.createTeacher(teacherDTO));
    }

    // --- 以下是供教师使用的接口 ---

    /**
     * 获取当前登录教师自己教授的课程列表
     * @param teacher Spring Security自动注入的当前登录教师对象
     * @return 该教师的课程列表
     */
    @GetMapping("/me/courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<List<CourseResponseDTO>> getMyCourses(@AuthenticationPrincipal Teacher teacher) {
        // 调用CourseService中我们之前创建的、根据教师ID查找课程的方法
        return ApiResult.success(courseService.findCoursesByTeacherId(teacher.getId()));
    }
}