package com.example.employeeweb.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Lưu mật khẩu đã mã hóa, không lưu mật khẩu gốc
    @Column(nullable = false)
    private String password;

    // Lưu "USER" hoặc "ADMIN", không thêm tiền tố ROLE_
    @Column(nullable = false, length = 20)
    private String role = "USER";

    public AppUser() {
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}