package com.example.sampleec.presentation.response;

import com.example.sampleec.authentication.entity.User;

/**
 * ユーザー情報レスポンス DTO。
 * passwordHash は絶対に含めない。
 */
public record UserData(
        String id,
        String email,
        String name,
        String companyName,
        String role
) {
    public static UserData from(User user) {
        return new UserData(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCompanyName(),
                user.getRole().toLowerCaseValue()
        );
    }
}
