package org.example.backend.foodpick.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.user.dto.UserResponse;
import org.example.backend.foodpick.domain.user.dto.WarningUpdateReqeust;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.model.UserStatus;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {
    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;

    public ResponseEntity<ApiResponse<List<UserResponse>>> getUserAll(String token){
        Long myId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(myId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        List<UserEntity> users = userRepository.findAll();

        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::of)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(200, "모든 유저들의 데이터입니다.", userResponses));
    }

    public ResponseEntity<ApiResponse<String>> warningUpdate(
            String token,
            Long userId,
            WarningUpdateReqeust request
    ) {

        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        int addWarning = request.getWarnings();

        if (addWarning < 1 || addWarning > 5) {
            throw new CustomException(ErrorException.INVALID_WARNING_RANGE);
        }

        int newWarning = user.getWarnings() + addWarning;
        String message = request.getMessage();
        user.updateWarning(newWarning, message);

        LocalDateTime banEndAt = calculateBanDuration(newWarning);

        if (banEndAt != null) {
            user.updateStatus(UserStatus.SUSPENDED, banEndAt);
        }

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse<>(200, "해당 유저의 경고 누적 및 제재 처리가 완료되었습니다.", null));
    }

    private LocalDateTime calculateBanDuration(int warning) {
        switch (warning) {
            case 2:
                return LocalDateTime.now().plusDays(1);
            case 3:
                return LocalDateTime.now().plusDays(3);
            case 4:
                return LocalDateTime.now().plusDays(7);
            case 5:
                return LocalDateTime.now().plusDays(30);
            default:
                if (warning >= 6) {
                    return LocalDateTime.MAX;
                }
                return null;
        }
    }
}
