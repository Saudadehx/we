package com.example.student_management_system.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import lombok.Data;

@Data
public class Teacher implements UserDetails {

    private Long id;
    private String teacherId; // 教师工号
    private String name;
    private String password;

    /**
     * 返回用户的权限集合。对于教师，我们硬编码返回 ROLE_TEACHER。
     * Spring Security会根据这个来判断用户是否有权访问特定API。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(Role.ROLE_TEACHER.name()));
    }

    /**
     * 返回用于认证的用户名。对于教师，我们使用其唯一的工号。
     */
    @Override
    public String getUsername() {
        return this.teacherId;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    // --- 以下方法默认返回true，表示账户始终有效 ---
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}