package com.example.sampleec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EC サイトバックエンド Spring Boot アプリケーション。
 * basePackages を明示して全モジュールの Bean をスキャンする。
 */
@SpringBootApplication(scanBasePackages = "com.example.sampleec")
public class EcApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcApplication.class, args);
    }
}
