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

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private CourseService courseService;

    // --- 管理员接口 ---
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<List<TeacherDTO>> getAllTeachers() {
        return ApiResult.success(teacherService.getAllTeachers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<TeacherDTO> createTeacher(@RequestBody TeacherDTO teacherDTO) {
        return ApiResult.success(teacherService.createTeacher(teacherDTO));
    }

    /**
     * 【新增】更新教师信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<TeacherDTO> updateTeacher(@PathVariable Long id, @RequestBody TeacherDTO teacherDTO) {
        return ApiResult.success(teacherService.updateTeacher(id, teacherDTO));
    }

    /**
     * 【新增】删除教师
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<?> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ApiResult.success();
    }


    // --- 教师接口 ---
    @GetMapping("/me/courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<List<CourseResponseDTO>> getMyCourses(@AuthenticationPrincipal Teacher teacher) {
        return ApiResult.success(courseService.findCoursesByTeacherId(teacher.getId()));
    }
}