package com.example.student_management_system.mapper;

import com.example.student_management_system.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    // 根据用户名查找用户，用于登录验证
    User findByUsername(String username);

    // 插入一个新用户，用于注册和数据加载
    void insert(User user);
}