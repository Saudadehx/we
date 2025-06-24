package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.model.Classroom;
import com.example.student_management_system.service.ClassroomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@PreAuthorize("hasRole('ADMIN')")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;

    @GetMapping
    public ApiResult<List<Classroom>> getAllClassrooms() {
        return ApiResult.success(classroomService.getAllClassrooms());
    }

    @PostMapping
    public ApiResult<Classroom> createClassroom(@RequestBody Classroom classroom) {
        return ApiResult.success(classroomService.createClassroom(classroom));
    }

    @PutMapping("/{id}")
    public ApiResult<Classroom> updateClassroom(@PathVariable Long id, @RequestBody Classroom classroom) {
        return ApiResult.success(classroomService.updateClassroom(id, classroom));
    }

    @DeleteMapping("/{id}")
    public ApiResult<?> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ApiResult.success();
    }
}