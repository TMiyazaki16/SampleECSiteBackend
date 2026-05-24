package com.example.sampleec.infrastructure.repository;

import com.example.sampleec.authentication.entity.Role;
import com.example.sampleec.authentication.entity.User;
import com.example.sampleec.authentication.repository.UserRepository;
import com.example.sampleec.infrastructure.entity.UserTableEntity;
import com.example.sampleec.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository の MyBatis 実装。
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        UserTableEntity entity = userMapper.findByEmail(email);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        UserTableEntity entity = userMapper.findById(id);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public void save(User user) {
        UserTableEntity entity = toEntity(user);
        userMapper.insert(entity);
    }

    private User toDomain(UserTableEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPassword())
                .name(entity.getName())
                .companyName(entity.getCompanyName())
                .role(Role.valueOf(entity.getRole()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private UserTableEntity toEntity(User user) {
        UserTableEntity entity = new UserTableEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPasswordHash());
        entity.setName(user.getName());
        entity.setCompanyName(user.getCompanyName());
        entity.setRole(user.getRole().name());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}
