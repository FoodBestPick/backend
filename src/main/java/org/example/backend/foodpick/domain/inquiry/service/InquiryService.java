package org.example.backend.foodpick.domain.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryCreateRequest;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryListResponse;
import org.example.backend.foodpick.domain.inquiry.model.InquiryEntity;
import org.example.backend.foodpick.domain.inquiry.repository.InquiryRepository;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.repository.UserRepository;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.s3.service.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final S3Service s3Service;

    @Transactional
    public ResponseEntity<ApiResponse<InquiryListResponse>> createInquiry(
            InquiryCreateRequest request,
            List<MultipartFile> files,
            String token
    ) {
        Long userId = jwtTokenValidator.getUserId(token);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        List<String> imageUrls = List.of();
        if (files != null && !files.isEmpty()) {
            ResponseEntity<ApiResponse<List<String>>> s3Resp = s3Service.uploadToS3(files);
            if (s3Resp != null && s3Resp.getBody() != null && s3Resp.getBody().getData() != null) {
                imageUrls = s3Resp.getBody().getData();
            }
        }

        InquiryEntity inquiry = InquiryEntity.create(
                user,
                request.getCategory(),
                request.getTitle(),
                request.getContent(),
                imageUrls
        );

        inquiryRepository.save(inquiry);

        return ResponseEntity.ok(new ApiResponse<>(200, "문의 작성 성공", InquiryListResponse.from(inquiry)));
    }

    public ResponseEntity<ApiResponse<List<InquiryListResponse>>> getMyInquiries(String token) {
        Long userId = jwtTokenValidator.getUserId(token);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        List<InquiryListResponse> list = inquiryRepository
                .findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(InquiryListResponse::from)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(200, "내 문의 목록 조회 성공", list));
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteInquiry(Long inquiryId, String token) {
        Long userId = jwtTokenValidator.getUserId(token);

        InquiryEntity inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorException.INQUIRY_NOT_FOUND));

        if (!inquiry.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorException.NO_PERMISSION);
        }

        List<String> urls = inquiry.getImages();
        if (urls != null && !urls.isEmpty()) {
            s3Service.deleteByUrls(urls);
        }

        inquiryRepository.delete(inquiry);

        return ResponseEntity.ok(new ApiResponse<>(200, "문의 삭제 성공", null));
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteAllInquirys(String token) {
        Long userId = jwtTokenValidator.getUserId(token);

        List<InquiryEntity> inquiries = inquiryRepository.findAllByUser_Id(userId);

        if (inquiries.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "삭제할 문의가 없습니다.", null));
        }

        List<String> allUrls = inquiries.stream()
                .flatMap(i -> i.getImages() == null ? java.util.stream.Stream.empty() : i.getImages().stream())
                .filter(u -> u != null && !u.isBlank())
                .distinct()
                .toList();

        if (!allUrls.isEmpty()) {
            s3Service.deleteByUrls(allUrls);
        }

        inquiryRepository.deleteAll(inquiries);

        return ResponseEntity.ok(new ApiResponse<>(200, "내 문의 전체 삭제 성공", null));
    }
}
