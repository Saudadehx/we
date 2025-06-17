package com.example.student_management_system.auth;

import com.example.student_management_system.mapper.UserMapper;
import com.example.student_management_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 导入 Slf4j
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException; // 导入 AuthenticationException
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
@RequiredArgsConstructor
@Slf4j // 使用 Lombok 的 Slf4j 注解
public class AuthenticationService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = new com.example.student_management_system.model.User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        userMapper.insert(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            log.debug("准备认证用户: {}", request.getUsername()); // 使用日志

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            log.debug("用户 {} 认证成功!", request.getUsername()); // 使用日志

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            log.debug("成功加载UserDetails, 用户名: {}, 权限: {}", userDetails.getUsername(), userDetails.getAuthorities()); // 使用日志

            log.debug("准备生成JWT令牌..."); // 使用日志
            var jwtToken = jwtService.generateToken(userDetails);
            log.debug("JWT令牌生成成功!"); // 使用日志

            return AuthenticationResponse.builder().token(jwtToken).build();

        } catch (AuthenticationException e) { // 捕获更具体的认证异常
            log.warn("认证失败: {}", e.getMessage()); // 使用日志
            throw e; // 重新抛出认证异常，由全局异常处理器处理
        } catch (Exception e) {
            log.error("认证过程中发生未知错误", e); // 使用日志记录完整堆栈
            throw new RuntimeException("认证服务内部错误", e); // 抛出通用运行时异常
        }
    }
}