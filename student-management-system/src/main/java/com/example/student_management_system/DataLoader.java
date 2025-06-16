package com.example.student_management_system;

import com.example.student_management_system.model.Role;
import com.example.student_management_system.model.User;
import com.example.student_management_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring会自动把UserRepository和PasswordEncoder注入进来
    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 检查数据库中是否已有admin用户，避免重复创建
        if (userRepository.findByUsername("admin").isEmpty()) {
            System.out.println("正在创建初始管理员账户...");

            User adminUser = new User();
            adminUser.setUsername("admin");
            // 使用我们配置的加密器来加密密码
            adminUser.setPassword(passwordEncoder.encode("password123"));
            // 设置正确的角色
            adminUser.setRole(Role.ROLE_ADMIN);

            userRepository.save(adminUser);

            System.out.println("初始管理员账户创建成功。用户名: admin, 密码: password123");
        }
    }
}