package org.example.backend.foodpick.domain.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.restaurant.dto.PictureResponse;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.restaurant.model.RestaurantPicture;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantPictureRepository;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantPictureService {
    private final RestaurantPictureRepository pictureRepository;
    private final RestaurantRepository restaurantRepository;

    // ✅ CREATE: URL 리스트로 DB에 사진 정보 저장
    @Transactional
    public ResponseEntity<ApiResponse<List<PictureResponse>>> addPictures(
            Long restaurantId, List<String> urls) {
        
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElse(null);
        if (restaurant == null) {
            return ResponseEntity.ok(ApiResponse.failure("식당을 찾을 수 없습니다.", 404));
        }

        List<PictureResponse> responses = urls.stream()
                .map(url -> {
                    RestaurantPicture picture = RestaurantPicture.builder()
                            .url(url)
                            .restaurant(restaurant)
                            .build();
                    pictureRepository.save(picture);
                    return PictureResponse.builder()
                            .id(picture.getId())
                            .url(picture.getUrl())
                            .build();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(200, "사진이 추가되었습니다.", responses));
    }

    // ✅ READ: 모든 사진 조회
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PictureResponse>>> getAllPictures(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            return ResponseEntity.ok(ApiResponse.failure("식당을 찾을 수 없습니다.", 404));
        }

        List<PictureResponse> responses = pictureRepository.findAllByRestaurant_Id(restaurantId)
                .stream()
                .map(p -> PictureResponse.builder()
                        .id(p.getId())
                        .url(p.getUrl())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(200, "사진 목록을 조회했습니다.", responses));
    }

    // ✅ DELETE: DB에서만 삭제 (S3는 별도 처리)
    @Transactional
    public ResponseEntity<ApiResponse<String>> deletePicture(Long restaurantId, Long pictureId) {
        RestaurantPicture picture = pictureRepository
                .findByIdAndRestaurant_Id(pictureId, restaurantId)
                .orElse(null);
        
        if (picture == null) {
            return ResponseEntity.ok(ApiResponse.failure("사진을 찾을 수 없습니다.", 404));
        }

        String url = picture.getUrl();
        pictureRepository.delete(picture);
        
        // ✅ S3 URL 반환 (Controller에서 S3Service 호출용)
        return ResponseEntity.ok(new ApiResponse<>(200, "사진이 삭제되었습니다.", url));
    }

    // ✅ 식당 삭제 시 모든 사진 URL 반환 (S3 일괄 삭제용)
    @Transactional(readOnly = true)
    public List<String> getAllPictureUrls(Long restaurantId) {
        return pictureRepository.findAllByRestaurant_Id(restaurantId)
                .stream()
                .map(RestaurantPicture::getUrl)
                .collect(Collectors.toList());
    }
}