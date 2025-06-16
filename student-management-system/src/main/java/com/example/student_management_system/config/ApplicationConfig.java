package com.example.student_management_system.config;

import com.example.student_management_system.mapper.StudentMapper;
import com.example.student_management_system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 【关键修改】恢复对管理员(user)表的查找
            UserDetails user = userMapper.findByUsername(username);
            if (user != null) {
                // 如果在管理员表中找到了，就直接返回
                return user;
            }

            // 如果不是管理员，再尝试作为学生在students表中查找
            UserDetails student = studentMapper.findByStudentId(username);
            if (student != null) {
                // 如果在学生表中找到了，就返回
                return student;
            }

            // 如果两个表都找不到，才抛出异常
            throw new UsernameNotFoundException("在任何表中都未找到用户: " + username);
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}