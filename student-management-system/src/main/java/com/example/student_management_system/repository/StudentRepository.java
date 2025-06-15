package com.example.student_management_system.repository;

import com.example.student_management_system.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
// Import List if using methods that return List<Student>
// import java.util.List;

@Repository // 声明这是一个Repository Bean
public interface StudentRepository extends JpaRepository<Student, Long> {

    // JpaRepository 已经提供了基本的 CRUD 方法，例如:
    // findAll(), findById(), save(), deleteById() 等

    // 我们可以根据需要添加自定义的查询方法
    // 例如，通过学号查询学生 (Spring Data JPA会根据方法名自动生成查询)
    Optional<Student> findByStudentId(String studentId);

    // 例如，通过姓名模糊查询学生列表
    // List<Student> findByNameContaining(String name);

    // 例如，通过班级名称查询学生列表
    // List<Student> findByClassName(String className);
}
