package com.example.sampleec.infrastructure.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * users テーブルのマッピング POJO。
 */
@Data
public class UserTableEntity {
    private String id;
    private String email;
    private String password;
    private String name;
    private String companyName;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
