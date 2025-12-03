package org.example.backend.foodpick.domain.restaurant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final RestaurantTimeRepository timeRepository; // ✅ 필수
    private final TagRepository tagRepository;
    private final TagBridgeRepository tagBridgeRepository;
    private final FoodRepository foodRepository;
    private final FoodBridgeRepository foodBridgeRepository;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    // ✅ 맛집 등록
    @Transactional
    public RestaurantResponse create(RestaurantCreateRequest request, List<MultipartFile> files, String token) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("토큰을 입력해주세요.");
        if (request.getRestaurant_name() == null || request.getRestaurant_name().isBlank()) throw new IllegalArgumentException("식당 이름을 입력해주세요.");

        try {
            // 1. 기본 정보 저장
            Restaurant restaurant = Restaurant.builder()
                    .name(request.getRestaurant_name())
                    .introduce(request.getRestaurant_introduce())
                    .address(request.getRestaurant_address())
                    .latitude(request.getRestaurant_latitude() != null ? request.getRestaurant_latitude().toString() : "0")
                    .longitude(request.getRestaurant_longitude() != null ? request.getRestaurant_longitude().toString() : "0")
                    .count(0L)
                    .build();
            
            Restaurant savedRestaurant = restaurantRepository.save(restaurant);

            // 2. 메뉴 저장 (JSON Parsing)
            if (request.getMenus() != null && !request.getMenus().isBlank()) {
                try {
                    String menusJson = request.getMenus().trim();
                    if (menusJson.startsWith("{")) {
                        menusJson = "[" + menusJson + "]";
                    }
                    List<MenuRequest> menuRequests = objectMapper.readValue(menusJson, new TypeReference<List<MenuRequest>>() {});
                    for (MenuRequest mr : menuRequests) {
                        if(mr.getMenu_name() == null || mr.getMenu_name().isBlank()) continue;
                        RestaurantMenu menu = RestaurantMenu.builder()
                                .restaurant(savedRestaurant)
                                .name(mr.getMenu_name())
                                .price(mr.getMenu_price())
                                .build();
                        menuRepository.save(menu);
                    }
                } catch (Exception e) {
                    log.error("메뉴 파싱 실패", e);
                }
            }

            // ✅ 3. 운영시간 저장 (JSON Parsing) - 여기가 핵심
            if (request.getTimes() != null && !request.getTimes().isBlank()) {
                try {
                    String timesJson = request.getTimes().trim();
                    if (timesJson.startsWith("{")) {
                        timesJson = "[" + timesJson + "]";
                    }
                    List<TimeRequest> timeRequests = objectMapper.readValue(timesJson, new TypeReference<List<TimeRequest>>() {});
                    for (TimeRequest tr : timeRequests) {
                        RestaurantTime time = RestaurantTime.builder()
                                .restaurant(savedRestaurant)
                                .week(tr.getWeek())
                                .startTime(tr.getStartTime())
                                .endTime(tr.getEndTime())
                                .restTime(tr.getRestTime())
                                .build();
                        timeRepository.save(time);
                    }
                } catch (Exception e) {
                    log.error("운영시간 파싱 실패", e);
                }
            }

            // 4. 태그 저장
            if (request.getTags() != null) {
                for (String tagName : request.getTags()) {
                    if (tagName == null || tagName.isBlank()) continue;
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                    TagBridge bridge = TagBridge.builder().restaurant(savedRestaurant).tag(tag).build();
                    tagBridgeRepository.save(bridge);
                }
            }

            // 5. 카테고리 저장
            if (request.getCategories() != null && !request.getCategories().isBlank()) {
                List<String> categoryList = new ArrayList<>();
                try {
                    String catJson = request.getCategories().trim();
                    if (catJson.startsWith("[")) {
                        categoryList = objectMapper.readValue(catJson, new TypeReference<List<String>>() {});
                    } else {
                        categoryList.add(catJson);
                    }
                } catch (Exception e) {
                    categoryList.add(request.getCategories());
                }

                for (String categoryName : categoryList) {
                    if (categoryName == null || categoryName.isBlank()) continue;
                    Food food = foodRepository.findByName(categoryName)
                            .orElseGet(() -> foodRepository.save(Food.builder().name(categoryName).build()));
                    FoodBridge fb = FoodBridge.builder().restaurant(savedRestaurant).food(food).build();
                    foodBridgeRepository.save(fb);
                }
            }

            // 6. 이미지 업로드
            if (files != null && !files.isEmpty()) {
                ResponseEntity<ApiResponse<List<String>>> s3Resp = s3Service.uploadToS3(files);
                if (s3Resp != null && s3Resp.getBody() != null && s3Resp.getBody().getData() != null) {
                    List<String> urls = s3Resp.getBody().getData();
                    for (String url : urls) {
                        RestaurantPicture picture = RestaurantPicture.builder()
                                .restaurant(savedRestaurant)
                                .url(url)
                                .build();
                        pictureRepository.save(picture);
                    }
                }
            }

            return RestaurantResponse.from(savedRestaurant);

        } catch (Exception e) {
            log.error("create error", e);
            throw new RuntimeException("맛집 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // ✅ 맛집 수정
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateRestaurant(Long id, RestaurantUpdateRequest req, List<MultipartFile> files, String accessToken) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));
        
        r.updateName(req.getRestaurant_name());
        r.updateIntroduce(req.getRestaurant_introduce());
        r.updateAddress(req.getRestaurant_address());
        r.updateLatitude(req.getRestaurant_latitude() != null ? req.getRestaurant_latitude().toString() : "0");
        r.updateLongitude(req.getRestaurant_longitude() != null ? req.getRestaurant_longitude().toString() : "0");

        // 메뉴 업데이트
        if (req.getMenus() != null && !req.getMenus().isBlank()) {
            try {
                menuRepository.deleteAllByRestaurant_Id(r.getId());
                String menusJson = req.getMenus().trim();
                if (menusJson.startsWith("{")) {
                    menusJson = "[" + menusJson + "]";
                }
                List<MenuRequest> menuRequests = objectMapper.readValue(menusJson, new TypeReference<List<MenuRequest>>() {});
                for (MenuRequest mr : menuRequests) {
                    menuRepository.save(RestaurantMenu.builder()
                            .restaurant(r).name(mr.getMenu_name()).price(mr.getMenu_price()).build());
                }
            } catch (Exception e) { log.error("메뉴 수정 오류", e); }
        }

        // ✅ 운영시간 업데이트 (기존 삭제 후 재등록)
        if (req.getTimes() != null && !req.getTimes().isBlank()) {
            try {
                timeRepository.deleteAllByRestaurant_Id(r.getId());
                String timesJson = req.getTimes().trim();
                if (timesJson.startsWith("{")) {
                    timesJson = "[" + timesJson + "]";
                }
                List<TimeRequest> timeRequests = objectMapper.readValue(timesJson, new TypeReference<List<TimeRequest>>() {});
                for (TimeRequest tr : timeRequests) {
                    timeRepository.save(RestaurantTime.builder()
                            .restaurant(r)
                            .week(tr.getWeek())
                            .startTime(tr.getStartTime())
                            .endTime(tr.getEndTime())
                            .restTime(tr.getRestTime())
                            .build());
                }
            } catch (Exception e) { log.error("운영시간 수정 오류", e); }
        }

        // 카테고리 업데이트
        if (req.getCategories() != null && !req.getCategories().isBlank()) {
            foodBridgeRepository.deleteAllByRestaurant_Id(r.getId());
            List<String> categoryList = new ArrayList<>();
            try {
                String catJson = req.getCategories().trim();
                if (catJson.startsWith("[")) {
                    categoryList = objectMapper.readValue(catJson, new TypeReference<List<String>>() {});
                } else {
                    categoryList.add(catJson);
                }
            } catch (Exception e) {
                categoryList.add(req.getCategories());
            }

            for (String catName : categoryList) {
                Food food = foodRepository.findByName(catName)
                        .orElseGet(() -> foodRepository.save(Food.builder().name(catName).build()));
                foodBridgeRepository.save(FoodBridge.builder().restaurant(r).food(food).build());
            }
        }

        // 태그 업데이트
        if (req.getTags() != null) {
            tagBridgeRepository.deleteAllByRestaurant_Id(r.getId());
            for (String tname : req.getTags()) {
                Tag tag = tagRepository.findByName(tname)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tname).build()));
                tagBridgeRepository.save(TagBridge.builder().restaurant(r).tag(tag).build());
            }
        }

        // 사진 삭제
        if (req.getDelete_picture_ids() != null && !req.getDelete_picture_ids().isEmpty()) {
            List<RestaurantPicture> toDelete = pictureRepository.findAllById(req.getDelete_picture_ids());
            for (RestaurantPicture p : toDelete) {
                try { s3Service.deleteFromS3(p.getUrl()); } catch (Exception ignored) {}
            }
            pictureRepository.deleteAll(toDelete);
        }

        // 사진 추가
        if (files != null && !files.isEmpty()) {
            ResponseEntity<ApiResponse<List<String>>> s3Resp = s3Service.uploadToS3(files);
            if (s3Resp != null && s3Resp.getBody() != null) {
                for (String url : s3Resp.getBody().getData()) {
                    pictureRepository.save(RestaurantPicture.builder().restaurant(r).url(url).build());
                }
            }
        }

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ✅ 맛집 삭제
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteRestaurant(Long id, String accessToken) {
        Restaurant r = restaurantRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));
        menuRepository.deleteAllByRestaurant_Id(r.getId());
        pictureRepository.deleteAllByRestaurant_Id(r.getId());
        timeRepository.deleteAllByRestaurant_Id(r.getId());
        tagBridgeRepository.deleteAllByRestaurant_Id(r.getId());
        foodBridgeRepository.deleteAllByRestaurant_Id(r.getId());
        restaurantRepository.delete(r);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ✅ 목록 조회
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> listRestaurants(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(restaurantRepository.findAll(pageable).map(RestaurantResponse::from)));
    }

    // ✅ 상세 조회
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("식당을 찾을 수 없습니다."));
        return ResponseEntity.ok(ApiResponse.success(RestaurantResponse.from(restaurant)));
    }

    // ✅ 이름 검색
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> searchByName(String name, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(restaurantRepository.findByNameContaining(name, pageable).map(RestaurantResponse::from)));
    }

    // ✅ [통합 검색] 키워드, 카테고리, 태그, 가격 필터
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> searchRestaurants(
            String keyword, String category, List<String> tags, Integer minPrice, Integer maxPrice) {
        
        List<Restaurant> all = restaurantRepository.findAll();
        
        List<RestaurantResponse> filtered = all.stream()
            .filter(r -> (keyword == null || keyword.isBlank()) || (r.getName().contains(keyword) || (r.getAddress() != null && r.getAddress().contains(keyword))))
            .filter(r -> (category == null || category.isBlank()) || foodBridgeRepository.findAllByRestaurant_Id(r.getId()).stream().anyMatch(fb -> fb.getFood().getName().contains(category)))
            .filter(r -> (tags == null || tags.isEmpty()) || tags.stream().anyMatch(t -> tagBridgeRepository.findAllByRestaurant_Id(r.getId()).stream().map(tb -> tb.getTag().getName()).anyMatch(rt -> rt.equals(t))))
            .filter(r -> {
                if (minPrice == null && maxPrice == null) return true;
                List<RestaurantMenu> menus = menuRepository.findAllByRestaurant_Id(r.getId());
                if (menus.isEmpty()) return false;
                return menus.stream().anyMatch(m -> {
                    if (m.getPrice() == null) return false;
                    int p = m.getPrice();
                    return (minPrice == null || p >= minPrice) && (maxPrice == null || p <= maxPrice);
                });
            })
            .map(RestaurantResponse::from)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(filtered));
    }
}