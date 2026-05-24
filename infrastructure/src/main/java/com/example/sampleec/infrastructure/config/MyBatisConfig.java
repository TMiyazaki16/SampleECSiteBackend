package com.example.sampleec.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 設定。
 * Mapper インターフェースのスキャン対象を明示する。
 */
@Configuration
@MapperScan("com.example.sampleec.infrastructure.mapper")
public class MyBatisConfig {
}
