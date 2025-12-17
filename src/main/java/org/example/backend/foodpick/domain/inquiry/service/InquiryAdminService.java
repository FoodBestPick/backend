package org.example.backend.foodpick.domain.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryAnswerRequest;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryListResponse;
import org.example.backend.foodpick.domain.inquiry.model.InquiryEntity;
import org.example.backend.foodpick.domain.inquiry.model.InquiryStatus;
import org.example.backend.foodpick.domain.inquiry.repository.InquiryRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryAdminService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;

    @Transactional
    public ResponseEntity<ApiResponse<String>> answerInquiry(Long inquiryId, InquiryAnswerRequest request, String token) {
        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        InquiryEntity inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorException.INQUIRY_NOT_FOUND));

        String adminContent = request.getAdminContent();
        if (adminContent == null || adminContent.isBlank()) {
            throw new CustomException(ErrorException.INQUIRY_INVALID_INPUT);
        }

        inquiry.answer(adminContent);

        inquiryRepository.save(inquiry);

        return ResponseEntity.ok(new ApiResponse<>(200, "문의 답변 완료", null));
    }

    public ResponseEntity<ApiResponse<List<InquiryListResponse>>> getInquiries(String token, InquiryStatus status) {
        Long adminId = jwtTokenValidator.getUserId(token);

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));
        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        List<InquiryEntity> list = (status == null)
                ? inquiryRepository.findAllByOrderByUpdatedAtDesc()
                : inquiryRepository.findByStatusOrderByUpdatedAtDesc(status);

        List<InquiryListResponse> res = list.stream()
                .map(InquiryListResponse::from)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(200, "관리자 문의 목록 조회 성공", res));
    }
}
