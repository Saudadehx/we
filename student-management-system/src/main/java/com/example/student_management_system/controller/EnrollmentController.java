package com.example.student_management_system.controller;

import com.example.student_management_system.dto.ApiResult;
import com.example.student_management_system.dto.EnrollmentDTO;
import com.example.student_management_system.dto.EnrollmentResponseDTO;
import com.example.student_management_system.dto.GradeUpdateDTO;
import com.example.student_management_system.model.CourseOffering;
import com.example.student_management_system.model.Enrollment;
import com.example.student_management_system.model.Student;
import com.example.student_management_system.model.Teacher;
import com.example.student_management_system.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping("/teachers/me/courses/{offeringId}/enrollments")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<List<EnrollmentResponseDTO>> getEnrollmentsForTeacher(
            @PathVariable Long offeringId,
            @AuthenticationPrincipal Teacher teacher
    ) {
        return ApiResult.success(enrollmentService.getEnrollmentsByOfferingForTeacher(offeringId, teacher.getId()));
    }

    @PutMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<?> updateGrade(
            @PathVariable Long enrollmentId,
            @RequestBody GradeUpdateDTO dto,
            @AuthenticationPrincipal Teacher teacher
    ) {
        enrollmentService.updateGrade(enrollmentId, dto.getScore(), teacher.getId());
        return ApiResult.success();
    }

    @GetMapping("/students/me/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<List<EnrollmentResponseDTO>> getMyEnrollments(@AuthenticationPrincipal Student student) {
        return ApiResult.success(enrollmentService.getEnrollmentsForStudent(student.getId()));
    }

    @PostMapping("/students/me/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<Enrollment> enrollInCourse(
            @AuthenticationPrincipal Student student,
            @RequestBody Map<String, Long> payload
    ) {
        Long offeringId = payload.get("courseId");
        if (offeringId == null) {
            throw new IllegalArgumentException("请求体中必须包含 courseId (课程安排ID)。");
        }
        return ApiResult.success(enrollmentService.enrollCourseForStudent(offeringId, student.getId()));
    }

    @DeleteMapping("/students/me/enrollments/{enrollmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<?> dropCourse(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal Student student
    ) {
        enrollmentService.dropCourse(enrollmentId, student.getId());
        return ApiResult.success();
    }

    @GetMapping("/students/me/available-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<List<CourseOffering>> getStudentAvailableCourses(@AuthenticationPrincipal Student student) {
        return ApiResult.success(enrollmentService.getAvailableOfferingsForStudent(student.getId()));
    }

    @PostMapping("/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Enrollment> createEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {
        return ApiResult.success(enrollmentService.createEnrollment(enrollmentDTO));
    }
}