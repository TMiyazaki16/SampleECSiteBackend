package com.example.sampleec.infrastructure.mapper;

import com.example.sampleec.infrastructure.entity.UserTableEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * users テーブルの MyBatis Mapper。
 */
@Mapper
public interface UserMapper {

    UserTableEntity findByEmail(@Param("email") String email);

    UserTableEntity findById(@Param("id") String id);

    void insert(UserTableEntity entity);
}
