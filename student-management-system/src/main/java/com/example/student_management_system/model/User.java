package com.example.student_management_system.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user") // 对应数据库的'user'表
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid") // 对应'userid'列
    private Long id;

    @Column(name = "username", unique = true, nullable = false) // 对应'username'列
    private String username;

    @Column(name = "password") // 对应'password'列
    private String password;

    @Enumerated(EnumType.STRING) // 将枚举类型以字符串形式存入数据库
    private Role role;

    // Getters and Setters ... (此处省略，请使用IDE自动生成)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }


    // 以下是实现UserDetails接口所必须的方法

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回用户的角色列表
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // 以下方法为了简单起见，全部返回true。在实际项目中可用于账户锁定等功能。
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