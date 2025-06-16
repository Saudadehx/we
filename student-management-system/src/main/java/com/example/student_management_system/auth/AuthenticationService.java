package com.example.student_management_system.auth;

import com.example.student_management_system.mapper.UserMapper; // 修改点1: 导入 UserMapper
import com.example.student_management_system.model.User;
// import com.example.student_management_system.repository.UserRepository; // 修改点2: 移除 UserRepository
import com.example.student_management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    // 修改点3: 将 UserRepository 替换为 UserMapper
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // 注册逻辑
    public AuthenticationResponse register(RegisterRequest request) {
        var user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        // 修改点4: 调用 mapper 的 insert 方法
        userMapper.insert(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    // 登录逻辑
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        // 修改点5: 调用 mapper 的 findByUsername 方法，并处理 null 情况
        var user = userMapper.findByUsername(request.getUsername());
        // 在实际项目中，如果认证成功，这里 user 不会为 null，但作为良好实践，可以添加检查
        if (user == null) {
            // 理论上 AuthenticationManager 认证失败会抛异常，走不到这里
            throw new IllegalStateException("Authentication passed but user not found.");
        }
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}