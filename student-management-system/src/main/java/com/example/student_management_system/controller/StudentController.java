package com.example.student_management_system.controller;

import com.example.student_management_system.model.Student;
import com.example.student_management_system.service.StudentService;
// This import will only work if ResourceNotFoundException is made public in StudentService OR moved to its own file.
import com.example.student_management_system.service.ResourceNotFoundException;
import jakarta.validation.Valid; // 用于启用对Student对象的校验
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import com.example.student_management_system.dto.DashboardStatsDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController // 结合了 @Controller 和 @ResponseBody，表示所有方法返回JSON/XML等
@RequestMapping("/api/students") // 此控制器下所有API的基础路径
public class StudentController {

    private final StudentService studentService;

    @Autowired // 构造函数注入 StudentService
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(studentService.getDashboardStats());
    }
    // GET /api/students - 获取所有学生
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students); // 返回 200 OK 和学生列表
    }

    // GET /api/students/{id} - 根据ID获取单个学生
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentService.getStudentById(id);
        return student.map(ResponseEntity::ok) // 如果找到，返回 200 OK 和学生信息
                .orElse(ResponseEntity.notFound().build()); // 否则返回 404 Not Found
    }

    // GET /api/students/by-studentid/{studentId} - 根据学号获取单个学生
    @GetMapping("/by-studentid/{studentId}")
    public ResponseEntity<Student> getStudentByStudentId(@PathVariable String studentId) {
        Optional<Student> student = studentService.getStudentByStudentId(studentId);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/students - 创建新学生
    @PostMapping
    public ResponseEntity<?> createStudent(@Valid @RequestBody Student student) {
        // @Valid 会触发Student实体中定义的校验规则
        // 如果校验失败，会抛出MethodArgumentNotValidException，由下面的handleValidationExceptions处理
        try {
            Student createdStudent = studentService.createStudent(student);
            return new ResponseEntity<>(createdStudent, HttpStatus.CREATED); // 返回 201 Created 和创建的学生信息
        } catch (IllegalArgumentException e) {
            // 处理Service层抛出的学号已存在等业务异常
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // PUT /api/students/{id} - 更新现有学生
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody Student studentDetails) {
        try {
            Student updatedStudent = studentService.updateStudent(id, studentDetails);
            return ResponseEntity.ok(updatedStudent); // 返回 200 OK 和更新后的学生信息
        } catch (ResourceNotFoundException e) { // Catching the specific exception
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // DELETE /api/students/{id} - 删除学生
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) { // Changed ResponseEntity<Void> to ResponseEntity<?> to allow body on error
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.noContent().build(); // 返回 204 No Content
        } catch (ResourceNotFoundException e) { // Catching the specific exception
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // 全局异常处理 - 处理@Valid校验失败的异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 当抛出MethodArgumentNotValidException时，返回400状态码
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors; // 返回包含字段错误信息的Map
    }

    // （可选）可以添加一个更通用的异常处理器来处理其他未捕获的异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        // Log the exception details here
        // ex.printStackTrace(); // Good for debugging, but use a logger in production
        return new ResponseEntity<>("发生内部服务器错误: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
