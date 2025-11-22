package org.example.backend.foodpick.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.auth.dto.*;
import org.example.backend.foodpick.domain.user.dto.LoginUserResponse;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.auth.repository.AuthRepository;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenProvider;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailService emailService;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

    public ResponseEntity<ApiResponse<TokenResponse>> signIn(AuthInRequest request) {

        if (request.getEmail() == null || !request.getEmail().matches(EMAIL_REGEX)) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        UserEntity user = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorException.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);
        authRepository.save(user);

        TokenResponse data = new TokenResponse(accessToken);

        LoginUserResponse userInfo = new LoginUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole() == UserRole.ADMIN
        );

        return ResponseEntity.ok(new AuthInResponse<>(200, "로그인 되었습니다.", data, userInfo)
        );
    }

    public ResponseEntity<ApiResponse<String>> emailSend(EmailSendRequest request) {

        if (request.getEmail() == null || !request.getEmail().matches(EMAIL_REGEX)) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

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

}
