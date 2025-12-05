package org.example.backend.foodpick.domain.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.restaurant.dto.MenuRequest;
import org.example.backend.foodpick.domain.restaurant.dto.MenuResponse;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.restaurant.service.RestaurantMenuService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RestaurantMenuController {
    private final RestaurantMenuService menuService;

    @PostMapping("/restaurant/{restaurantId}/menus")
    public ResponseEntity<ApiResponse<Void>> addMenu(
            @PathVariable("restaurantId") Long restaurantId,
            @RequestBody MenuRequest request) {
        return menuService.addMenu(restaurantId, request);
    }

    @GetMapping("/restaurant/{restaurantId}/menus")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenus(@PathVariable("restaurantId") Long restaurantId) {
        return menuService.getMenusByRestaurant(restaurantId);
    }

    @DeleteMapping("/restaurant/{restaurantId}/menus/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(
            @PathVariable("restaurantId") Long restaurantId,
            @PathVariable("menuId") Long menuId) {
        return menuService.deleteMenu(restaurantId, menuId);
    }

    /**
     * ✅ 메뉴명으로 음식점 검색
     * GET /menus/search?name=김치찌개&page=0&size=10
     */
    @GetMapping("/menus/search")
    public ResponseEntity<ApiResponse<Page<Restaurant>>> searchByMenu(
            @RequestParam("name") String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "id") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        return menuService.searchRestaurantsByMenu(name, pageable);
    }
}