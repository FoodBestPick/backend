package org.example.backend.foodpick.domain.tag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.foodpick.domain.restaurant.dto.RestaurantResponse;
import org.example.backend.foodpick.domain.tag.dto.TagRequest;
import org.example.backend.foodpick.domain.tag.dto.TagResponse;
import org.example.backend.foodpick.domain.tag.model.Tag;
import org.example.backend.foodpick.domain.tag.model.TagBridge;
import org.example.backend.foodpick.domain.tag.repository.TagBridgeRepository;
import org.example.backend.foodpick.domain.tag.repository.TagRepository;
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
public class TagService {

    private final TagRepository tagRepository;
    private final TagBridgeRepository tagBridgeRepository;

    @Transactional
    public ResponseEntity<ApiResponse<Void>> createTag(TagRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("태그 이름을 입력해주세요.", 400));
        }
        Optional<Tag> existing = tagRepository.findByName(request.getName());
        if (existing.isPresent()) {
            return ResponseEntity.ok(ApiResponse.failure("이미 존재하는 태그입니다.", 400));
        }
        Tag tag = Tag.builder().name(request.getName()).category(request.getCategory()).build();
        tagRepository.save(tag);
        return ResponseEntity.ok(new ApiResponse<>(200, "태그가 생성되었습니다.", null));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        List<TagResponse> responses = tags.stream()
                .map(t -> TagResponse.builder().id(t.getId()).name(t.getName()).category(t.getCategory()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(200, "태그 목록", responses));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTagsByCategory(String category) {
        List<Tag> tags = tagRepository.findByCategory(category);
        List<TagResponse> responses = tags.stream()
                .map(t -> TagResponse.builder().id(t.getId()).name(t.getName()).category(t.getCategory()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(200, "카테고리별 태그 목록", responses));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateTag(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("태그를 찾을 수 없습니다.")); // ✅ 안전한 예외 처리
        tag.updateName(request.getName());
        tag.updateCategory(request.getCategory());
        return ResponseEntity.ok(new ApiResponse<>(200, "태그가 수정되었습니다.", null));
    }

    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("태그를 찾을 수 없습니다.")); // ✅ 안전한 예외 처리
        tagBridgeRepository.deleteAllByTag_Id(id);
        tagRepository.delete(tag);
        return ResponseEntity.ok(new ApiResponse<>(200, "태그가 삭제되었습니다.", null));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<RestaurantResponse>>> searchRestaurantsByTag(String tagName, Pageable pageable) {
        if (tagName == null || tagName.isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("검색할 태그명을 입력해주세요.", 400));
        }
        Page<Tag> tagPage = tagRepository.findByNameContaining(tagName, pageable);
        if (tagPage.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(200, "태그 검색 결과", Page.empty(pageable)));
        }
        List<Long> tagIds = tagPage.getContent().stream().map(Tag::getId).collect(Collectors.toList());
        List<TagBridge> bridges = tagBridgeRepository.findAllByTag_IdIn(tagIds);
        
        List<RestaurantResponse> responses = bridges.stream()
                .map(TagBridge::getRestaurant)
                .distinct()
                .map(RestaurantResponse::from)
                .collect(Collectors.toList());

        Page<RestaurantResponse> restaurantPage = new PageImpl<>(responses, pageable, tagPage.getTotalElements());
        return ResponseEntity.ok(new ApiResponse<>(200, "태그 검색 결과", restaurantPage));
    }
}