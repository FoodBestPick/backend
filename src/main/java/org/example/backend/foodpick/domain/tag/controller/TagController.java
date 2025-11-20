package org.example.backend.foodpick.domain.tag.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.restaurant.model.Restaurant;
import org.example.backend.foodpick.domain.tag.dto.TagRequest;
import org.example.backend.foodpick.domain.tag.dto.TagResponse;
import org.example.backend.foodpick.domain.tag.service.TagService;
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
@RequestMapping("/tag")
public class TagController {
    private final TagService tagService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createTag(@RequestBody TagRequest request) {
        return tagService.createTag(request);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        return tagService.getAllTags();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateTag(
            @PathVariable Long id,
            @RequestBody TagRequest request) {
        return tagService.updateTag(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        return tagService.deleteTag(id);
    }

    /**
     * ✅ 태그명으로 음식점 검색
     * GET /tag/search?name=맛집&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Restaurant>>> searchByTag(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        return tagService.searchRestaurantsByTag(name, pageable);
    }
}