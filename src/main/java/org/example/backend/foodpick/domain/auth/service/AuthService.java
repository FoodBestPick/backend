package org.example.backend.foodpick.domain.auth.service;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.auth.dto.*;
import org.example.backend.foodpick.domain.user.dto.LoginUserResponse;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.auth.repository.AuthRepository;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.model.UserStatus;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenProvider;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.redis.service.RedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailService emailService;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenValidator jwtTokenValidator;
    private final RedisService redisService;

    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";

    public ResponseEntity<ApiResponse<String>> signUp(AuthUpRequest request) {

        if (request.getEmail() == null || !request.getEmail().matches(EMAIL_REGEX)) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        if (!emailService.isVerified(request.getEmail())) {
            throw new CustomException(ErrorException.EMAIL_VERTIFICATION);
        }

        if (authRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorException.DUPLICATE_EMAIL);
        }

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new CustomException(ErrorException.INVALID_PASSWORD);
        }

        if (authRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorException.DUPLICATE_NICKNAME);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserEntity user = UserEntity.ofSignUp(
                request.getEmail(),
                encodedPassword,
                request.getNickname()
        );

        authRepository.save(user);

        emailService.clearVerified(request.getEmail());

        return ResponseEntity.ok(new ApiResponse<>(200, "회원가입이 완료되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<TokenResponse>> signIn(
            AuthInRequest request,
            HttpServletResponse response
    ) {

        if (request.getEmail() == null || !request.getEmail().matches(EMAIL_REGEX)) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.SUSPENDED) {

            LocalDateTime now = LocalDateTime.now();

            // 영구정지 (ban_end_at == LocalDateTime.MAX)
            if (user.getBanEndAt() != null && user.getBanEndAt().equals(LocalDateTime.MAX)) {
                throw new CustomException(ErrorException.PERMANENTLY_BANNED);
            }

            // 기간 정지 중 (ban_end_at 미래 날짜)
            if (user.getBanEndAt() != null && user.getBanEndAt().isAfter(now)) {
                throw new CustomException(ErrorException.TEMP_BANNED);
            }

            // 정지 기간이 지났으면 자동 복구
            if (user.getBanEndAt() != null && user.getBanEndAt().isBefore(now)) {
                user.clearBan();
                authRepository.save(user);
            }
        }


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorException.INVALID_PASSWORD);
        }

        redisService.recordLogin(user.getId(), user.getRole());
        redisService.recordVisit(user.getId(), user.getRole());

        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.updatedAt(LocalDateTime.now().withNano(0));
        authRepository.save(user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24 * 14) // 14일
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        TokenResponse data = new TokenResponse(accessToken);

        LoginUserResponse userInfo = new LoginUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole() == UserRole.ADMIN
        );

        return ResponseEntity.ok(new AuthInResponse<>(200, "로그인 되었습니다.", data, userInfo));
    }


    public ResponseEntity<ApiResponse<String>> emailSendSignUp(EmailSendRequest request) {

        if (authRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorException.DUPLICATE_EMAIL);
        }

        if (request.getEmail() == null || !request.getEmail().matches(EMAIL_REGEX)) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        String authCode = emailService.generateAuthCode();

        emailService.saveAuthCode(request.getEmail(), authCode);
        emailService.sendAuthCode(request.getEmail(), authCode);

        return ResponseEntity.ok(new ApiResponse<>(200, "이메일 인증번호가 전송되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<String>> emailSendResetPassword(EmailSendRequest request) {

        if (request.getEmail() == null || !request.getEmail().matches(EMAIL_REGEX)) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        String authCode = emailService.generateAuthCode();
        emailService.saveAuthCode(request.getEmail(), authCode);
        emailService.sendAuthCode(request.getEmail(), authCode);

        return ResponseEntity.ok(new ApiResponse<>(200, "이메일 인증번호가 전송되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<String>> emailVerify(EmailVerifyRequest request) {
        if (request.getEmail() == null || !request.getEmail().matches(EMAIL_REGEX)) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new CustomException(ErrorException.EMAIL_NOT_VERIFIED);
        }

        emailService.verifyCode(request.getEmail(), request.getCode());
        emailService.markVerified(request.getEmail());

        return ResponseEntity.ok(new ApiResponse<>(200, "이메일 인증번호가 확인되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<String>> resetPassword(ResetPasswordRequest request) {

        if (request.getEmail() == null || !request.getEmail().matches(EMAIL_REGEX)) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        if (request.getPassword() == null || request.getPasswordConfirm() == null) {
            throw new CustomException(ErrorException.PASSWORD_NOT_VERIFIED);
        }

        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new CustomException(ErrorException.PASSWORD_NOT_CONFIRM);
        }

        if (!emailService.isVerified(request.getEmail())) {
            throw new CustomException(ErrorException.EMAIL_VERTIFICATION);
        }

        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorException.SAME_PASSWORD_NOT_ALLOWED);
        }

        String encoded = passwordEncoder.encode(request.getPassword());
        user.updatePassword(encoded);

        authRepository.save(user);

        emailService.clearVerified(request.getEmail());

        return ResponseEntity.ok(new ApiResponse<>(200, "비밀번호가 변경되었습니다.", null));
    }

    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            HttpServletRequest request
    ) {

        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            throw new CustomException(ErrorException.INVALID_REFRESH_TOKEN);
        }

        jwtTokenValidator.validateRefreshToken(refreshToken);

        Long userId = jwtTokenValidator.getUserIdFromRefreshToken(refreshToken);

        UserEntity user = authRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new CustomException(ErrorException.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.generateToken(user.getId());

        TokenResponse response = new TokenResponse(newAccessToken);

        return ResponseEntity.ok(new ApiResponse<>(200, "액세스 토큰 재발급 완료", response));
    }

}
