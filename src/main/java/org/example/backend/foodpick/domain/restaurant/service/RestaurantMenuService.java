package org.example.backend.foodpick.domain.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.restaurant.dto.MenuRequest;
import org.example.backend.foodpick.domain.restaurant.dto.MenuResponse;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.restaurant.model.RestaurantMenu;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantMenuRepository;
import org.example.backend.foodpick.domain.restaurant.repository.RestaurantRepository;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantMenuService {

    private final RestaurantMenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public ResponseEntity<ApiResponse<Void>> addMenu(Long restaurantId, MenuRequest request) {
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        if (restaurantOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.failure("음식점을 찾을 수 없습니다.", 404));
        }

        Optional<RestaurantMenu> existingMenu = menuRepository.findByRestaurant_IdAndName(restaurantId, request.getMenu_name());
        if (existingMenu.isPresent()) {
            return ResponseEntity.ok(ApiResponse.failure("이미 등록된 메뉴입니다.", 409));
        }

        RestaurantMenu menu = RestaurantMenu.builder()
                .name(request.getMenu_name())
                .price(request.getMenu_price()) // ✅ 가격 추가
                .restaurant(restaurantOpt.get())
                .build();

        menuRepository.save(menu);
        return ResponseEntity.ok(new ApiResponse<>(200, "메뉴가 추가되었습니다.", null));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenusByRestaurant(Long restaurantId) {
        List<RestaurantMenu> menus = menuRepository.findAllByRestaurant_Id(restaurantId);
        List<MenuResponse> responses = menus.stream()
                .map(m -> MenuResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .price(m.getPrice()) // ✅ 가격 추가
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(200, "메뉴 목록", responses));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteMenu(Long restaurantId, Long menuId) {
        Optional<RestaurantMenu> menuOpt = menuRepository.findById(menuId);
        if (menuOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.failure("메뉴를 찾을 수 없습니다.", 404));
        }

        RestaurantMenu menu = menuOpt.get();
        if (!menu.getRestaurant().getId().equals(restaurantId)) {
            return ResponseEntity.ok(ApiResponse.failure("해당 음식점의 메뉴가 아닙니다.", 400));
        }

        menuRepository.delete(menu);
        return ResponseEntity.ok(new ApiResponse<>(200, "메뉴가 삭제되었습니다.", null));
    }

    /**
     * ✅ 메뉴명으로 음식점 검색 (페이징)
     */
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<Restaurant>>> searchRestaurantsByMenu(String menuName, Pageable pageable) {
        if (menuName == null || menuName.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("검색할 메뉴명을 입력해주세요.", 400));
        }

        // 1. 메뉴명으로 메뉴 검색 (페이징)
        Page<RestaurantMenu> menuPage = menuRepository.findByNameContaining(menuName, pageable);
        
        // 2. 메뉴에서 Restaurant 추출
        List<Restaurant> restaurants = menuPage.getContent().stream()
                .map(RestaurantMenu::getRestaurant)
                .distinct()
                .collect(Collectors.toList());

        // 3. Page로 변환
        Page<Restaurant> restaurantPage = new PageImpl<>(
                restaurants,
                pageable,
                menuPage.getTotalElements()
        );

        return ResponseEntity.ok(new ApiResponse<>(200, "메뉴 검색 결과", restaurantPage));
    }
}