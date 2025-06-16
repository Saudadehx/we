package com.example.student_management_system.repository;

import com.example.student_management_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA会根据方法名自动生成查询
    // 我们需要一个通过用户名查找用户的方法
    Optional<User> findByUsername(String username);
}