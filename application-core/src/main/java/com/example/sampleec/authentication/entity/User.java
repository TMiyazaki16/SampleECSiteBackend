package com.example.sampleec.authentication.entity;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * ユーザーエンティティ。
 * ID による同一性を持つドメインオブジェクト。
 */
@Value
@Builder
public class User {

    String id;
    String email;
    /** BCryptハッシュ化されたパスワード。外部へは絶対に公開しない。 */
    String passwordHash;
    String name;
    String companyName;
    Role role;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
