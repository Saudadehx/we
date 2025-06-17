package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.MajorDTO;
import com.example.student_management_system.service.MajorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/majors")
@PreAuthorize("hasRole('ADMIN')") // 整个Controller都需要管理员权限
public class MajorController {

    @Autowired
    private MajorService majorService;

    @GetMapping
    public ApiResult<List<MajorDTO>> getAllMajors() {
        return ApiResult.success(majorService.getAllMajors());
    }

    @PostMapping
    public ApiResult<MajorDTO> createMajor(@Valid @RequestBody MajorDTO majorDTO) {
        return ApiResult.success(majorService.createMajor(majorDTO));
    }

    @PutMapping("/{id}")
    public ApiResult<MajorDTO> updateMajor(@PathVariable Long id, @Valid @RequestBody MajorDTO majorDTO) {
        return ApiResult.success(majorService.updateMajor(id, majorDTO));
    }

    @DeleteMapping("/{id}")
    public ApiResult<?> deleteMajor(@PathVariable Long id) {
        majorService.deleteMajor(id);
        return ApiResult.success();
    }
}