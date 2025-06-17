package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.CourseCatalogDTO; // ✨ 修改：导入DTO
import com.example.student_management_system.model.CourseOffering;
import com.example.student_management_system.service.CourseService;
import jakarta.validation.Valid; // ✨ 新增：导入校验注解
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
    public ApiResult<List<CourseCatalogDTO>> getAllCatalogs() { // ✨ 修改：返回DTO
        return ApiResult.success(courseService.getAllCatalogs());
    }

    @PostMapping("/course-catalogs")
    public ApiResult<CourseCatalogDTO> createCatalog(@Valid @RequestBody CourseCatalogDTO catalog) { // ✨ 修改：接收DTO
        return ApiResult.success(courseService.createCatalog(catalog));
    }

    // ✨ 新增：更新课程目录接口
    @PutMapping("/course-catalogs/{id}")
    public ApiResult<CourseCatalogDTO> updateCatalog(@PathVariable Long id, @Valid @RequestBody CourseCatalogDTO catalog) {
        return ApiResult.success(courseService.updateCatalog(id, catalog));
    }

    // ✨ 新增：删除课程目录接口
    @DeleteMapping("/course-catalogs/{id}")
    public ApiResult<?> deleteCatalog(@PathVariable Long id) {
        courseService.deleteCatalog(id);
        return ApiResult.success();
    }

    // --- 课程安排接口 (保持不变) ---
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