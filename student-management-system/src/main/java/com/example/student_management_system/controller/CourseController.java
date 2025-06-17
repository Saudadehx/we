package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.model.CourseCatalog;
import com.example.student_management_system.model.CourseOffering;
import com.example.student_management_system.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // --- 课程目录接口 ---
    @GetMapping("/course-catalogs")
    public ApiResult<List<CourseCatalog>> getAllCatalogs() {
        return ApiResult.success(courseService.getAllCatalogs());
    }

    @PostMapping("/course-catalogs")
    public ApiResult<CourseCatalog> createCatalog(@RequestBody CourseCatalog catalog) {
        return ApiResult.success(courseService.createCatalog(catalog));
    }

    // --- 课程安排接口 ---
    @GetMapping("/course-offerings")
    public ApiResult<List<CourseOffering>> getAllOfferings() {
        return ApiResult.success(courseService.getAllOfferings());
    }

    @PostMapping("/course-offerings")
    public ApiResult<CourseOffering> createOffering(@RequestBody CourseOffering offering) {
        return ApiResult.success(courseService.createOffering(offering));
    }

    @PutMapping("/course-offerings/{id}")
    public ApiResult<CourseOffering> updateOffering(@PathVariable Long id, @RequestBody CourseOffering offering) {
        offering.setId(id);
        return ApiResult.success(courseService.updateOffering(offering));
    }

    @DeleteMapping("/course-offerings/{id}")
    public ApiResult<?> deleteOffering(@PathVariable Long id) {
        courseService.deleteOffering(id);
        return ApiResult.success();
    }
}