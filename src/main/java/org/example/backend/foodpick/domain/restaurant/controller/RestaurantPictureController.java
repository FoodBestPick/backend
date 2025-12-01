package org.example.backend.foodpick.domain.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.restaurant.dto.PictureResponse;
import org.example.backend.foodpick.domain.restaurant.service.RestaurantPictureService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.s3.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurant/{restaurantId}/pictures")
public class RestaurantPictureController {
    private final RestaurantPictureService pictureService;
    private final S3Service s3Service;

    // CREATE: URL 리스트로 DB 저장
    @PostMapping
    public ResponseEntity<ApiResponse<List<PictureResponse>>> add(
            @PathVariable Long restaurantId,
            @RequestBody List<String> urls) {
        return pictureService.addPictures(restaurantId, urls);
    }

    // READ: 모든 사진 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<PictureResponse>>> getAll(@PathVariable Long restaurantId) {
        return pictureService.getAllPictures(restaurantId);
    }

    // DELETE: DB + S3 삭제
    @DeleteMapping("/{pictureId}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable Long restaurantId,
            @PathVariable Long pictureId) {
        
        ResponseEntity<ApiResponse<String>> response = pictureService.deletePicture(restaurantId, pictureId);
        
        // S3 삭제 (URL 반환되었을 경우)
        if (response.getBody().getData() != null) {
            try {
                s3Service.deleteFromS3(response.getBody().getData());
            } catch (Exception e) {
                // S3 삭제 실패해도 DB는 이미 삭제됨
            }
        }
        
        return ResponseEntity.ok(new ApiResponse<>(200, "사진이 삭제되었습니다.", null));
    }
}