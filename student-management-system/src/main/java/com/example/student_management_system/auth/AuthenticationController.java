package com.example.student_management_system.auth;

import com.example.student_management_system.dto.ApiResult; // ✨ 1. 导入我们创建的 ApiResult
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    // ✨ 2. 将返回类型从 ResponseEntity<AuthenticationResponse> 修改为 ApiResult<AuthenticationResponse>
    @PostMapping("/register")
    public ApiResult<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        // ✨ 3. 调用 service 层获取业务数据，然后用 ApiResult.success()进行包装
        return ApiResult.success(service.register(request));
    }

    // ✨ 4. 对登录接口做同样修改
    @PostMapping("/authenticate")
    public ApiResult<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        // ✨ 5. 同样用 ApiResult.success() 进行包装
        return ApiResult.success(service.authenticate(request));
    }
}