package com.example.sampleec.presentation.controller;

import com.example.sampleec.authentication.entity.User;
import com.example.sampleec.authentication.service.AuthApplicationService;
import com.example.sampleec.presentation.config.JwtConfig;
import com.example.sampleec.presentation.request.LoginRequest;
import com.example.sampleec.presentation.response.ApiResponse;
import com.example.sampleec.presentation.response.AuthResponseData;
import com.example.sampleec.presentation.response.UserData;
import com.example.sampleec.presentation.response.UserResponseData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 認証コントローラー。
 */
@Tag(name = "Auth", description = "認証 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final JwtConfig jwtConfig;

    /**
     * ログイン。
     */
    @Operation(summary = "ログイン", description = "メールアドレスとパスワードで認証し、JWT トークンを返す")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseData>> login(
            @Valid @RequestBody LoginRequest request) {

        User user = authApplicationService.login(request.email(), request.password());
        String token = jwtConfig.generateToken(user.getId());

        AuthResponseData responseData = new AuthResponseData(UserData.from(user), token);
        return ResponseEntity.ok(ApiResponse.of(responseData));
    }

    /**
     * ログアウト（サーバー側はステートレスのため、クライアント側でトークン削除）。
     */
    @Operation(summary = "ログアウト", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> logout() {
        return ResponseEntity.ok(ApiResponse.of(Map.of("success", true)));
    }

    /**
     * 現在のログインユーザー情報を取得。
     */
    @Operation(summary = "ログインユーザー情報取得", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseData>> me(Authentication authentication) {
        String userId = authentication.getName();
        User user = authApplicationService.findById(userId);
        return ResponseEntity.ok(ApiResponse.of(new UserResponseData(UserData.from(user))));
    }
}
