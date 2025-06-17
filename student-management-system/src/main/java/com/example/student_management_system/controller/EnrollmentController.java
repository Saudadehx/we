package com.example.student_management_system.controller;

import com.example.student_management_system.dto.*;
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
@RequestMapping("/api") // 使用一个通用的API前缀
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    // --- 供教师使用的接口 ---

    /**
     * 教师获取其某门课程的学生花名册
     * @param courseId 课程的数据库ID
     * @param teacher 当前登录的教师Principal
     * @return 选课学生列表及成绩
     */
    @GetMapping("/teachers/me/courses/{courseId}/enrollments")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<List<EnrollmentResponseDTO>> getEnrollmentsForTeacher(
            @PathVariable Long courseId,
            @AuthenticationPrincipal Teacher teacher
    ) {
        // 异常处理已在Service层完成，Controller层直接调用即可
        return ApiResult.success(enrollmentService.getEnrollmentsByCourseForTeacher(courseId, teacher.getId()));
    }

    /**
     * 教师更新成绩
     * @param enrollmentId 选课记录的ID
     * @param dto 包含分数的DTO
     * @param teacher 当前登录的教师 Principal
     * @return 操作成功
     */
    @PutMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResult<?> updateGrade(
            @PathVariable Long enrollmentId,
            @RequestBody GradeUpdateDTO dto,
            @AuthenticationPrincipal Teacher teacher
    ) {
        // 异常处理已在Service层完成，Controller层直接调用即可
        enrollmentService.updateGrade(enrollmentId, dto.getScore(), teacher.getId());
        return ApiResult.success();
    }

    // --- 供学生使用的接口 ---

    /**
     * 学生获取自己的成绩单
     * @param student 当前登录的学生 Principal
     * @return 个人成绩列表
     */
    @GetMapping("/students/me/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<List<EnrollmentResponseDTO>> getMyEnrollments(@AuthenticationPrincipal Student student) {
        return ApiResult.success(enrollmentService.getEnrollmentsForStudent(student.getId()));
    }

    /**
     * 学生选课接口
     * @param student 当前登录的学生
     * @param payload 请求体，需要包含 courseId
     * @return 创建的选课记录
     */
    @PostMapping("/students/me/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<Enrollment> enrollInCourse(
            @AuthenticationPrincipal Student student,
            @RequestBody Map<String, Long> payload
    ) {
        Long courseId = payload.get("courseId");
        if (courseId == null) {
            // 这种基本的参数校验仍然可以在Controller层保留，因为它不涉及复杂的业务逻辑
            throw new IllegalArgumentException("请求体中必须包含 courseId。");
        }
        // 业务逻辑异常已在Service层处理，Controller层直接调用即可
        return ApiResult.success(enrollmentService.enrollCourseForStudent(courseId, student.getId()));
    }

    /**
     * 学生退课接口
     * @param enrollmentId 选课记录的数据库ID
     * @param student 当前登录的学生Principal
     * @return 操作成功
     */
    @DeleteMapping("/students/me/enrollments/{enrollmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<?> dropCourse(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal Student student
    ) {
        // 业务逻辑异常已在Service层处理，Controller层直接调用即可
        enrollmentService.dropCourse(enrollmentId, student.getId());
        return ApiResult.success();
    }

    /**
     * 【新增】学生获取自己可选的课程列表
     */
    @GetMapping("/students/me/available-courses")
    @PreAuthorize("hasRole('STUDENT')")
    public ApiResult<List<CourseResponseDTO>> getStudentAvailableCourses(@AuthenticationPrincipal Student student) {
        return ApiResult.success(enrollmentService.getAvailableCoursesForStudent(student.getId()));
    }

    // --- 供管理员使用的接口 ---
    @PostMapping("/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Enrollment> createEnrollment(@RequestBody EnrollmentDTO enrollmentDTO) {
        // 业务逻辑异常已在Service层处理，Controller层直接调用即可
        return ApiResult.success(enrollmentService.createEnrollment(enrollmentDTO));


    }
    /**
     * 新增：管理员为专业预置课程的接口
     */
    @PostMapping("/enrollments/assign-compulsory")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Map<String, String>> assignCourses(@RequestBody Map<String, Object> payload) {
        try {
            Long majorId = Long.parseLong(payload.get("majorId").toString());
            Integer academicYear = Integer.parseInt(payload.get("academicYear").toString());
            Integer semester = Integer.parseInt(payload.get("semester").toString());

            int count = enrollmentService.assignCompulsoryCourses(majorId, academicYear, semester);
            String message = "成功为 " + count + " 名学生分配了必修课。";
            return ApiResult.success(Map.of("message", message));

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("majorId, academicYear, semester 必须是有效的数字。");
        } catch (Exception e) {
            throw e; // 其他异常由全局处理器处理
        }
    }
}
