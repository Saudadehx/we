package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.ClassDTO;
import com.example.student_management_system.service.ClassService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@PreAuthorize("hasRole('ADMIN')")
public class ClassController {

    @Autowired
    private ClassService classService;

    @GetMapping
    public ApiResult<List<ClassDTO>> getAllClasses() {
        return ApiResult.success(classService.getAllClasses());
    }

    @PostMapping
    public ApiResult<ClassDTO> createClass(@Valid @RequestBody ClassDTO classDTO) {
        return ApiResult.success(classService.createClass(classDTO));
    }

    @PutMapping("/{id}")
    public ApiResult<ClassDTO> updateClass(@PathVariable Long id, @Valid @RequestBody ClassDTO classDTO) {
        return ApiResult.success(classService.updateClass(id, classDTO));
    }

    @DeleteMapping("/{id}")
    public ApiResult<?> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ApiResult.success();
    }
}