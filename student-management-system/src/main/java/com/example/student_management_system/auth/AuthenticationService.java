package com.example.student_management_system.auth;

import com.example.student_management_system.model.Role;
import com.example.student_management_system.model.User;
import com.example.student_management_system.repository.UserRepository;
import com.example.student_management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // 注册逻辑
    public AuthenticationResponse register(RegisterRequest request) {
        var user = new User(); // 使用var代替User，更简洁
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 加密密码
        user.setRole(request.getRole());
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    // 登录逻辑
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // authenticationManager会使用我们之前配置的AuthenticationProvider来验证用户
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        // 如果上面没有抛出异常，说明认证成功
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}