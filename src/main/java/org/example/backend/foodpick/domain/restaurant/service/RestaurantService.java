package org.example.backend.foodpick.domain.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.restaurant.dto.*;
import org.example.backend.foodpick.domain.restaurant.model.*;
import org.example.backend.foodpick.domain.restaurant.repository.*;
import org.example.backend.foodpick.domain.tag.model.Tag;
import org.example.backend.foodpick.domain.tag.repository.TagRepository;
import org.example.backend.foodpick.domain.tag.model.TagBridge;
import org.example.backend.foodpick.domain.tag.repository.TagBridgeRepository;
import org.example.backend.foodpick.domain.food.model.Food;
import org.example.backend.foodpick.domain.food.repository.FoodRepository;
import org.example.backend.foodpick.domain.food.model.FoodBridge;
import org.example.backend.foodpick.domain.food.repository.FoodBridgeRepository;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.s3.service.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RestaurantService {
    
    private final RestaurantRepository restaurantRepository;
    private final RestaurantMenuRepository menuRepository;
    private final RestaurantPictureRepository pictureRepository;
    private final RestaurantTimeRepository timeRepository;
    private final TagRepository tagRepository;
    private final TagBridgeRepository tagBridgeRepository;
    private final FoodRepository foodRepository;
    private final FoodBridgeRepository foodBridgeRepository;
    private final S3Service s3Service;
    
    /**
     * ✅ 맛집 등록 (가격 포함)
     */
    @Transactional
    public RestaurantResponse create(RestaurantCreateRequest request, List<MultipartFile> files, String token) {
        
        // 검증
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("토큰을 입력해주세요.");
        }

        if (request.getRestaurant_name() == null || request.getRestaurant_name().isBlank()) {
            throw new IllegalArgumentException("식당의 이름을 입력해주세요.");
        }

        if (request.getRestaurant_address() == null || request.getRestaurant_address().isBlank()) {
            throw new IllegalArgumentException("식당의 주소를 입력해주세요.");
        }

        if (request.getRestaurant_latitude() == null || request.getRestaurant_longitude() == null
                || request.getRestaurant_latitude().isBlank() || request.getRestaurant_longitude().isBlank()) {
            throw new IllegalArgumentException("식당의 좌표를 입력해주세요.");
        }

        // 중복 검사
        if (restaurantRepository.existsByNameAndAddress(request.getRestaurant_name(), request.getRestaurant_address())) {
            throw new IllegalArgumentException("이미 등록된 식당입니다.");
        }

        try {
            // 1. Restaurant 생성
            Restaurant restaurant = Restaurant.builder()
                    .name(request.getRestaurant_name())
                    .introduce(request.getRestaurant_introduce())
                    .address(request.getRestaurant_address())
                    .latitude(request.getRestaurant_latitude())
                    .longitude(request.getRestaurant_longitude())
                    .count(0L)
                    .build();
            
            Restaurant saved = restaurantRepository.save(restaurant);

            // 2. 이미지 업로드 및 저장
            if (files != null && !files.isEmpty()) {
                ResponseEntity<ApiResponse<List<String>>> s3Resp = s3Service.uploadToS3(files);
                if (s3Resp != null && s3Resp.getBody() != null && s3Resp.getBody().getData() != null) {
                    List<String> urls = s3Resp.getBody().getData();
                    List<RestaurantPicture> pics = urls.stream()
                            .map(url -> RestaurantPicture.builder()
                                    .url(url)
                                    .restaurant(saved)
                                    .build())
                            .collect(Collectors.toList());
                    if (!pics.isEmpty()) {
                        pictureRepository.saveAll(pics);
                    }
                }
            }

            // 3. 메뉴 저장 (가격 포함)
            if (request.getMenus() != null && !request.getMenus().isEmpty()) {
                List<RestaurantMenu> menus = request.getMenus().stream()
                        .filter(Objects::nonNull)
                        .filter(menuReq -> menuReq.getMenu_name() != null && !menuReq.getMenu_name().isBlank())
                        .map(menuReq -> RestaurantMenu.builder()
                                .name(menuReq.getMenu_name())
                                .price(menuReq.getMenu_price())
                                .restaurant(saved)
                                .build())
                        .collect(Collectors.toList());
                if (!menus.isEmpty()) {
                    menuRepository.saveAll(menus);
                }
            }

            // 4. 태그 저장
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                for (String tagName : request.getTags()) {
                    if (tagName == null || tagName.isBlank()) continue;
                    
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                    
                    TagBridge bridge = TagBridge.builder()
                            .restaurant(saved)
                            .tag(tag)
                            .build();
                    tagBridgeRepository.save(bridge);
                }
            }

            // 5. Food/Category 저장
            if (request.getRestaurant_category() != null && !request.getRestaurant_category().isBlank()) {
                String cat = request.getRestaurant_category();
                Food food = foodRepository.findByName(cat)
                        .orElseGet(() -> foodRepository.save(Food.builder().name(cat).build()));
                
                FoodBridge fb = FoodBridge.builder()
                        .restaurant(saved)
                        .food(food)
                        .build();
                foodBridgeRepository.save(fb);
            }

            return RestaurantResponse.from(saved);
            
        } catch (Exception e) {
            log.error("create error: {}", e.getMessage(), e);
            throw new RuntimeException("서버 내부에 오류가 발생했습니다.", e);
        }
    }

    /**
     * ✅ 맛집 수정
     */
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateRestaurant(Long id,
                                                              RestaurantUpdateRequest req,
                                                              List<MultipartFile> files,
                                                              String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("토큰을 입력해주세요.", 401));
        }

        Optional<Restaurant> opt = restaurantRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.failure("해당 식당을 찾을 수 없습니다.", 404));
        }

        try {
            Restaurant r = opt.get();
            
            // 기본 정보 업데이트
            if (req.getRestaurant_name() != null && !req.getRestaurant_name().isBlank()) {
                r.updateName(req.getRestaurant_name());
            }
            if (req.getRestaurant_introduce() != null) {
                r.updateIntroduce(req.getRestaurant_introduce());
            }
            if (req.getRestaurant_address() != null && !req.getRestaurant_address().isBlank()) {
                r.updateAddress(req.getRestaurant_address());
            }
            if (req.getRestaurant_latitude() != null && !req.getRestaurant_latitude().isBlank()) {
                r.updateLatitude(req.getRestaurant_latitude());
            }
            if (req.getRestaurant_longitude() != null && !req.getRestaurant_longitude().isBlank()) {
                r.updateLongitude(req.getRestaurant_longitude());
            }

            restaurantRepository.save(r);

            // 메뉴 업데이트 (가격 포함)
            if (req.getMenus() != null) {
                menuRepository.deleteAllByRestaurant_Id(r.getId());
                List<RestaurantMenu> menus = req.getMenus().stream()
                        .filter(Objects::nonNull)
                        .filter(menuReq -> menuReq.getMenu_name() != null && !menuReq.getMenu_name().isBlank())
                        .map(menuReq -> RestaurantMenu.builder()
                                .name(menuReq.getMenu_name())
                                .price(menuReq.getMenu_price())
                                .restaurant(r)
                                .build())
                        .collect(Collectors.toList());
                if (!menus.isEmpty()) {
                    menuRepository.saveAll(menus);
                }
            }

            // pictures 삭제
            if (req.getDelete_picture_ids() != null && !req.getDelete_picture_ids().isEmpty()) {
                List<Long> delIds = req.getDelete_picture_ids();
                List<RestaurantPicture> toDelete = pictureRepository.findAllById(delIds);
                if (!toDelete.isEmpty()) {
                    for (RestaurantPicture rp : toDelete) {
                        try {
                            s3Service.deleteFromS3(rp.getUrl());
                        } catch (Exception e) {
                            log.error("S3 delete error for {}: {}", rp.getUrl(), e.getMessage());
                        }
                    }
                    pictureRepository.deleteAll(toDelete);
                }
            }

            // 새 pictures 업로드
            if (files != null && !files.isEmpty()) {
                ResponseEntity<ApiResponse<List<String>>> s3Resp = s3Service.uploadToS3(files);
                if (s3Resp != null && s3Resp.getBody() != null && s3Resp.getBody().getData() != null) {
                    List<String> urls = s3Resp.getBody().getData();
                    List<RestaurantPicture> pics = urls.stream()
                            .map(url -> RestaurantPicture.builder().url(url).restaurant(r).build())
                            .collect(Collectors.toList());
                    if (!pics.isEmpty()) {
                        pictureRepository.saveAll(pics);
                    }
                }
            }

            // tags 업데이트
            if (req.getTags() != null) {
                tagBridgeRepository.deleteAllByRestaurant_Id(r.getId());
                for (String tname : req.getTags()) {
                    if (tname == null || tname.isBlank()) continue;
                    Tag tag = tagRepository.findByName(tname).orElseGet(() ->
                            tagRepository.save(Tag.builder().name(tname).build()));
                    TagBridge tb = TagBridge.builder().restaurant(r).tag(tag).build();
                    tagBridgeRepository.save(tb);
                }
            }

            // food/category 업데이트
            if (req.getRestaurant_category() != null && !req.getRestaurant_category().isBlank()) {
                foodBridgeRepository.deleteAllByRestaurant_Id(r.getId());
                String cat = req.getRestaurant_category();
                Food food = foodRepository.findByName(cat).orElseGet(() ->
                        foodRepository.save(Food.builder().name(cat).build()));
                FoodBridge fb = FoodBridge.builder().restaurant(r).food(food).build();
                foodBridgeRepository.save(fb);
            }

            return ResponseEntity.ok(new ApiResponse<>(200, "맛집 정보가 수정되었습니다.", null));
        } catch (Exception e) {
            log.error("updateRestaurant error: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.failure("서버 내부에 오류가 발생했습니다.", 500));
        }
    }

    /**
     * ✅ 맛집 삭제
     */
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(Long id, String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("토큰을 입력해주세요.", 401));
        }

        Optional<Restaurant> opt = restaurantRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.failure("해당 식당을 찾을 수 없습니다.", 404));
        }

        try {
            Restaurant r = opt.get();

            // S3에서 사진 먼저 삭제
            List<RestaurantPicture> pictures = pictureRepository.findAllByRestaurant_Id(r.getId());
            if (!pictures.isEmpty()) {
                for (RestaurantPicture pic : pictures) {
                    try {
                        s3Service.deleteFromS3(pic.getUrl());
                    } catch (Exception e) {
                        log.error("S3 삭제 오류: {}", e.getMessage());
                    }
                }
            }

            // DB에서 하위 데이터 삭제
            menuRepository.deleteAllByRestaurant_Id(r.getId());
            pictureRepository.deleteAllByRestaurant_Id(r.getId());
            timeRepository.deleteAllByRestaurant_Id(r.getId());
            tagBridgeRepository.deleteAllByRestaurant_Id(r.getId());
            foodBridgeRepository.deleteAllByRestaurant_Id(r.getId());

            restaurantRepository.delete(r);
            
            return ResponseEntity.ok(new ApiResponse<>(200, "맛집이 삭제되었습니다.", null));
        } catch (Exception e) {
            log.error("deleteRestaurant error: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.failure("서버 내부에 오류가 발생했습니다.", 500));
        }
    }

    /**
     * 맛집 목록 조회
     */
    public ResponseEntity<ApiResponse<Page<Restaurant>>> listRestaurants(Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findAll(pageable);
        return ResponseEntity.ok(new ApiResponse<>(200, "음식점 목록 조회 성공", restaurants));
    }

    /**
     * 맛집 상세 조회
     */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Restaurant>> getRestaurantById(Long id) {
        Optional<Restaurant> restaurant = restaurantRepository.findById(id);
        if (restaurant.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.failure("음식점을 찾을 수 없습니다.", 404));
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "음식점 조회 성공", restaurant.get()));
    }

    /**
     * 이름으로 검색
     */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<Restaurant>>> searchByName(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("검색할 음식점 이름을 입력해주세요.", 400));
        }
        
        Page<Restaurant> restaurants = restaurantRepository.findByNameContaining(name, pageable);
        return ResponseEntity.ok(new ApiResponse<>(200, "음식점 이름 검색 결과", restaurants));
    }

    /**
     * 주소로 검색
     */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<Restaurant>>> searchByAddress(String address, Pageable pageable) {
        if (address == null || address.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("검색할 주소를 입력해주세요.", 400));
        }
        
        Page<Restaurant> restaurants = restaurantRepository.findByAddressContaining(address, pageable);
        return ResponseEntity.ok(new ApiResponse<>(200, "주소 검색 결과", restaurants));
    }
}