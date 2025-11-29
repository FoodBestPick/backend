package org.example.backend.foodpick.domain.tag.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.tag.dto.TagRequest;
import org.example.backend.foodpick.domain.tag.dto.TagResponse;
import org.example.backend.foodpick.domain.tag.service.TagService;
import org.example.backend.foodpick.global.util.ApiResponse;
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
    
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTagsByCategory(@PathVariable("category") String category) {
        return tagService.getTagsByCategory(category);
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<ApiResponse<Void>> updateTag(
            @PathVariable("tagId") Long tagId, 
            @RequestBody TagRequest request) {
        return tagService.updateTag(tagId, request);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable("tagId") Long tagId) {
        return tagService.deleteTag(tagId);
    }
}