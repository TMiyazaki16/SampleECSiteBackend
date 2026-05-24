package com.example.sampleec.authentication.service;

import com.example.sampleec.authentication.entity.User;
import com.example.sampleec.authentication.repository.UserRepository;
import com.example.sampleec.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 認証アプリケーションサービス。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * ログイン処理。
     *
     * @param email       メールアドレス
     * @param rawPassword 生パスワード
     * @return 認証済みユーザー
     * @throws BusinessException 認証失敗時（401）
     */
    public User login(String email, String rawPassword) {
        log.info("ログイン試行: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("ユーザーが見つかりません: email={}", email);
                    return new BusinessException(HttpStatus.UNAUTHORIZED,
                            "メールアドレスまたはパスワードが正しくありません");
                });

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.warn("パスワード不一致: email={}", email);
            throw new BusinessException(HttpStatus.UNAUTHORIZED,
                    "メールアドレスまたはパスワードが正しくありません");
        }

        log.info("ログイン成功: userId={}", user.getId());
        return user;
    }

    /**
     * ユーザーID からユーザーを取得（JWT 検証後の呼び出し用）。
     *
     * @param userId ユーザーID
     * @return ユーザー
     * @throws BusinessException ユーザーが存在しない場合（401）
     */
    public User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "認証情報が無効です"));
    }
}
