package org.example.backend.foodpick.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.user.dto.UserProfileResponse;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.s3.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final S3Service s3Service;

    public ResponseEntity<ApiResponse<UserProfileResponse>> getUser(String token){
        Long myId = jwtTokenValidator.getUserId(token);

        if (myId == null) {
            throw new CustomException(ErrorException.USER_ID_NOT_FOUND);
        }

        UserEntity user = userRepository.findById(myId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        UserProfileResponse response = UserProfileResponse.of(user);

        return ResponseEntity.ok(new ApiResponse<>(200, "내 정보를 조회하였습니다.", response));
    }

    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(String token, Long id) {

        Long myId = jwtTokenValidator.getUserId(token);

        if (myId == null) {
            throw new CustomException(ErrorException.USER_ID_NOT_FOUND);
        }

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        UserProfileResponse response = UserProfileResponse.of(user);


        return ResponseEntity.ok(new ApiResponse<>(200, "상대방 정보를 조회하였습니다.", response)
        );
    }

    public ResponseEntity<ApiResponse<String>> editUserProfile(
            String token,
            MultipartFile file,
            String nickname,
            String stateMessage) {

        Long myId = jwtTokenValidator.getUserId(token);
        UserEntity user = userRepository.findById(myId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (file != null && !file.isEmpty()) {
            if (user.getImageUrl() != null) {
                try {
                    s3Service.deleteFromS3(user.getImageUrl());
                } catch (Exception e) {
                    throw new CustomException(ErrorException.FILE_DELETE_FAILED);
                }
            }

            // 새 파일 업로드
            try {
                String url = s3Service.uploadToS3Single(file);
                user.updateImageUrl(url);
            } catch (Exception e) {
                throw new CustomException(ErrorException.FILE_UPLOAD_FAILED);
            }
        }

        user.updateProfile(nickname, stateMessage);
        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse<>(200, "프로필이 수정되었습니다.", null));
    }
}
