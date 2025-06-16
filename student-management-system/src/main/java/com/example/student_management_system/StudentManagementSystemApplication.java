package com.example.student_management_system;

import org.mybatis.spring.annotation.MapperScan; // 1. 导入 @MapperScan
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.student_management_system.mapper") // 2. <-- 添加这一行
public class StudentManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentManagementSystemApplication.class, args);
	}

}