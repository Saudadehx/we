// file: student-management-system/src/main/java/com/example/student_management_system/StudentManagementSystemApplication.java

package com.example.student_management_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; // 1. 导入 @EnableAsync

@SpringBootApplication
@MapperScan("com.example.student_management_system.mapper")
@EnableAsync // 2. <-- 添加这一行，开启异步功能
public class StudentManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentManagementSystemApplication.class, args);
	}

}