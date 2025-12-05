package org.example.backend.foodpick.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.chat.dto.MatchingRequest;
import org.example.backend.foodpick.domain.chat.dto.MatchingResponse;
import org.example.backend.foodpick.domain.chat.service.MatchingService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/match")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping
    public ResponseEntity<ApiResponse<MatchingResponse>> requestMatch(
            @RequestHeader("Authorization") String token,
            @RequestBody MatchingRequest request
    ) {
        return matchingService.requestMatch(token, request);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> cancelMatch(@RequestHeader("Authorization") String token){
        return matchingService.cancelMatch(token);
    }
}
