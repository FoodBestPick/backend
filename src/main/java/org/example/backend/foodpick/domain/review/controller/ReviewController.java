package org.example.backend.foodpick.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.review.dto.ReviewRequest;
import org.example.backend.foodpick.domain.review.dto.ReviewResponse;
import org.example.backend.foodpick.domain.review.service.ReviewService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Review", description = "리뷰 API (작성/수정/삭제/조회)")
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "리뷰 작성",
            description = """
                    리뷰를 작성합니다. (multipart/form-data)
                    
                    - data 파트: ReviewRequest JSON 문자열
                    - file 파트: 리뷰 이미지 파일 리스트(선택)
                    
                    - data:
                      {"restaurantId":12,"content":"맛있어요!","rating":4.5,"images":[]}
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리뷰 작성 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(토큰 누락/만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Parameter(
                    description = "ReviewRequest JSON 문자열 (multipart 'data' 파트)",
                    required = true,
                    example = "{\"restaurantId\":12,\"content\":\"맛있어요!\",\"rating\":4.5,\"images\":[]}"
            )
            @RequestPart(value = "data") String data,

            @Parameter(
                    description = "리뷰 이미지 파일들 (multipart 'file' 파트, 선택)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart(value = "file", required = false) List<MultipartFile> files,

            @Parameter(
                    description = "Bearer 토큰 (예: Bearer eyJ...)",
                    required = true
            )
            @RequestHeader("Authorization") String token
    ) {
        try {
            ReviewRequest request = objectMapper.readValue(data, ReviewRequest.class);
            return reviewService.createReview(request, files, token);
        } catch (Exception e) {
            throw new RuntimeException("리뷰 등록 중 오류 발생: " + e.getMessage());
        }
    }

    @Operation(
            summary = "리뷰 수정",
            description = """
                    리뷰를 수정합니다. (multipart/form-data)
                    
                    - data 파트: ReviewRequest JSON 문자열
                    - file 파트: 리뷰 이미지 파일 리스트(선택)
                    
                    - data:
                      {"restaurantId":12,"content":"수정된 리뷰","rating":5.0,"images":[]}
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리뷰 수정 성공",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(토큰 누락/만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @Parameter(description = "리뷰 ID", required = true, example = "101")
            @PathVariable("id") Long id,

            @Parameter(
                    description = "ReviewRequest JSON 문자열 (multipart 'data' 파트)",
                    required = true,
                    example = "{\"restaurantId\":12,\"content\":\"수정된 리뷰\",\"rating\":5.0,\"images\":[]}"
            )
            @RequestPart(value = "data") String data,

            @Parameter(
                    description = "리뷰 이미지 파일들 (multipart 'file' 파트, 선택)",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart(value = "file", required = false) List<MultipartFile> files,

            @Parameter(
                    description = "Bearer 토큰 (예: Bearer eyJ...)",
                    required = true
            )
            @RequestHeader("Authorization") String token
    ) {
        try {
            ReviewRequest request = objectMapper.readValue(data, ReviewRequest.class);
            return reviewService.updateReview(id, request, files, token);
        } catch (Exception e) {
            throw new RuntimeException("리뷰 수정 중 오류 발생: " + e.getMessage());
        }
    }

    @Operation(
            summary = "리뷰 삭제",
            description = "리뷰를 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(토큰 누락/만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "리뷰 ID", required = true, example = "101")
            @PathVariable("id") Long id,

            @Parameter(
                    description = "Bearer 토큰 (예: Bearer eyJ...)",
                    required = true
            )
            @RequestHeader("Authorization") String token
    ) {
        return reviewService.deleteReview(id, token);
    }

    @Operation(
            summary = "맛집 리뷰 목록 조회",
            description = """
                    특정 맛집의 리뷰 목록을 페이지 형태로 조회합니다.
                    
                    - Authorization 헤더는 선택입니다.
                    - 토큰을 포함하면 응답의 isMine/isLiked 같은 사용자 기반 필드가 정확해질 수 있습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getRestaurantReviews(
            @Parameter(description = "맛집 ID", required = true, example = "12")
            @PathVariable("restaurantId") Long restaurantId,

            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,

            @Parameter(description = "Bearer 토큰 (선택, 있으면 isMine/isLiked 등에 반영)", required = false)
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        return reviewService.getRestaurantReviews(restaurantId, pageable, token);
    }

    @Operation(
            summary = "내 리뷰 목록 조회",
            description = "내가 작성한 리뷰 목록을 페이지 형태로 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패(토큰 누락/만료)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getMyReviews(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,

            @Parameter(
                    description = "Bearer 토큰 (예: Bearer eyJ...)",
                    required = true
            )
            @RequestHeader("Authorization") String token
    ) {
        return reviewService.getMyReviews(pageable, token);
    }
}
