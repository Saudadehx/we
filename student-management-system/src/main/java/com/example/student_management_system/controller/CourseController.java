package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.CourseDTO;
import com.example.student_management_system.dto.CourseResponseDTO;
import com.example.student_management_system.model.Course;
import com.example.student_management_system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 获取所有课程的列表，对所有已认证用户开放。
     * @return 课程列表
     */
    @GetMapping
    public ApiResult<List<CourseResponseDTO>> getAllCourses() {
        return ApiResult.success(courseService.getAllCoursesWithTeacherName());
    }

    /**
     * 创建一个新课程，仅限管理员。
     * @param courseDTO 课程数据
     * @return 创建的课程
     */
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
    // 此处可以添加 @PutMapping 和 @DeleteMapping 接口
}