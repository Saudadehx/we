package com.example.student_management_system;

import com.example.student_management_system.auth.AuthenticationRequest;
import com.example.student_management_system.auth.RegisterRequest;
import com.example.student_management_system.model.Role;
import com.example.student_management_system.model.Student;
import com.example.student_management_system.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc; // 用于模拟HTTP请求

    @Autowired
    private ObjectMapper objectMapper; // 用于将Java对象转换为JSON字符串

    @Autowired
    private UserRepository userRepository;

    // 在每个测试方法运行前，清空user表，确保测试之间互相独立
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegister_Success() throws Exception {
        // 1. 准备注册请求的数据
        var request = new RegisterRequest("testuser", "password", Role.ROLE_STUDENT);

        // 2. 模拟发送POST请求到 /api/auth/register
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // 3. 验证响应是否符合预期
                .andExpect(status().isOk()) // 期望HTTP状态码是 200 OK
                .andExpect(jsonPath("$.token").isNotEmpty()); // 期望返回的JSON中，token字段不为空
    }

    @Test
    void testAuthenticate_Success() throws Exception {
        // 1. 先注册一个用户，为登录做准备
        var registerRequest = new RegisterRequest("admin", "123123", Role.ROLE_ADMIN);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // 2. 准备登录请求的数据
        var authRequest = new AuthenticationRequest("admin", "123123");

        // 3. 模拟发送POST请求到 /api/auth/authenticate
        mockMvc.perform(post("/api/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void testAccessProtectedEndpoint_WithoutToken_ShouldFail() throws Exception {
        // 模拟发送GET请求到受保护的 /api/students 接口，但不带token
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden()); // 期望HTTP状态码是 403 Forbidden
    }

    @Test
    void testAdminCanPostStudent_And_StudentCannotPostStudent() throws Exception {
        // === 1. 管理员测试 (预期成功) ===
        // 注册并登录管理员，获取Token
        var adminRegister = new RegisterRequest("myadmin", "adminpass", Role.ROLE_ADMIN);
        MvcResult adminLoginResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegister)))
                .andReturn();
        String adminToken = objectMapper.readTree(adminLoginResult.getResponse().getContentAsString()).get("token").asText();

        // 准备一个新学生的数据
        var newStudent = new Student("S101", "新生", "女", LocalDate.now().minusYears(18), "测试班级");

        // 使用管理员Token尝试创建学生
        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + adminToken) // 在请求头中带上Token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isCreated()); // 期望HTTP状态码是 201 Created


        // === 2. 学生测试 (预期失败) ===
        // 注册并登录学生，获取Token
        var studentRegister = new RegisterRequest("mystudent", "studentpass", Role.ROLE_STUDENT);
        MvcResult studentLoginResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentRegister)))
                .andReturn();
        String studentToken = objectMapper.readTree(studentLoginResult.getResponse().getContentAsString()).get("token").asText();

        // 使用学生Token尝试创建同一个学生
        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + studentToken) // 在请求头中带上Token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isForbidden()); // 期望HTTP状态码是 403 Forbidden
    }
}