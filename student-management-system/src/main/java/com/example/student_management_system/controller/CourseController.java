package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.CourseDTO;
import com.example.student_management_system.dto.CourseResponseDTO;
import com.example.student_management_system.model.Course;
import com.example.student_management_system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 【修改】恢复为通用的课程列表接口，主要供管理员使用
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()") // 任何认证用户都可访问
    public ApiResult<List<CourseResponseDTO>> getAllCourses(
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String teacherName
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("courseName", courseName);
        params.put("courseId", courseId);
        params.put("teacherName", teacherName);
        return ApiResult.success(courseService.getAllCoursesWithTeacherName(params));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Course> createCourse(@RequestBody CourseDTO courseDTO) {
        return ApiResult.success(courseService.createCourse(courseDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Course> updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO) {
        return ApiResult.success(courseService.updateCourse(id, courseDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<?> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ApiResult.success();
    }
}