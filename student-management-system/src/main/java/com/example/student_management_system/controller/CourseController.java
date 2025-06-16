package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.CourseDTO;
import com.example.student_management_system.dto.CourseResponseDTO;
import com.example.student_management_system.model.Course;
import com.example.student_management_system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap; // 【新增】导入 HashMap
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 【修复】获取所有课程的列表，支持搜索
     * 将接收整个Map的方式，改为分别接收具体的、并且非必需的参数，以增强接口的健壮性。
     * 这样即使前端不传递任何参数，此接口也能正常响应。
     */
    @GetMapping
    public ApiResult<List<CourseResponseDTO>> getAllCourses(
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String teacherName
    ) {
        // 手动将接收到的参数放入Map中，再传递给Service层
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