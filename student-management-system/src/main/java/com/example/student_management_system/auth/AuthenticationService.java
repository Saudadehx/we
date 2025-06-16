package com.example.student_management_system.auth;

import com.example.student_management_system.mapper.UserMapper;
import com.example.student_management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService; // 1. 导入
import org.springframework.security.core.userdetails.UserDetails;    // 2. 导入
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService; // 3. 注入我们自定义的UserDetailsService

    // 注册管理员的逻辑保持不变
    public AuthenticationResponse register(RegisterRequest request) {
        var user = new com.example.student_management_system.model.User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        userMapper.insert(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    // 登录逻辑
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            System.out.println("--- DEBUG: 准备认证用户: " + request.getUsername());

            // 1. 认证步骤
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            System.out.println("--- DEBUG: 用户 " + request.getUsername() + " 认证成功!");

            // 2. 加载用户详情
            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            System.out.println("--- DEBUG: 成功加载UserDetails, 用户名: " + userDetails.getUsername() + ", 权限: " + userDetails.getAuthorities());

            // 3. 生成JWT令牌
            System.out.println("--- DEBUG: 准备生成JWT令牌...");
            var jwtToken = jwtService.generateToken(userDetails);
            System.out.println("--- DEBUG: JWT令牌生成成功!");

            return AuthenticationResponse.builder().token(jwtToken).build();

        } catch (Exception e) {
            // 4. 【关键】如果以上任何步骤出错，捕获异常并打印完整的错误信息
            System.err.println("!!!!!!!!!! 认证过程中发生致命错误 !!!!!!!!!!");
            e.printStackTrace(); // 这会把详细的错误“堆栈”打印到后端的控制台
            // 重新抛出异常，这样前端看到的现象（500错误）和原来一样
            throw new RuntimeException("认证服务内部错误", e);
        }
    }
}