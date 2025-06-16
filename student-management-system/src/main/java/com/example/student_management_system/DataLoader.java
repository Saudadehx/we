package com.example.student_management_system;

import com.example.student_management_system.mapper.UserMapper;
import com.example.student_management_system.model.Role;
import com.example.student_management_system.model.User;
// import com.example.student_management_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {


    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public DataLoader(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if (userMapper.findByUsername("admin") == null) {
            System.out.println("正在创建初始管理员账户...");

            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("password123"));
            adminUser.setRole(Role.ROLE_ADMIN);

            userMapper.insert(adminUser);

            System.out.println("初始管理员账户创建成功。用户名: admin, 密码: password123");
        }
    }
}