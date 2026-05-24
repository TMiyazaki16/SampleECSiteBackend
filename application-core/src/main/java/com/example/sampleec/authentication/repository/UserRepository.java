package com.example.sampleec.authentication.repository;

import com.example.sampleec.authentication.entity.User;

import java.util.Optional;

/**
 * ユーザーリポジトリインターフェース。
 * 実装は infrastructure 層に置く（DIP の実現）。
 */
public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);

    void save(User user);
}
