package org.example.backend.foodpick.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.auth.dto.*;
import org.example.backend.foodpick.domain.auth.service.OauthService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OauthController {

    private final OauthService oauthService;

    @PostMapping("/signin/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> signInKakao (@RequestBody TokenRequest request,
                                                                   HttpServletResponse response){
        return oauthService.signInKakao(request, response);
    }

    @PostMapping("/signin/google")
    public ResponseEntity<ApiResponse<TokenResponse>> signInGoogle (@RequestBody TokenRequest request,
                                                                    HttpServletResponse response){
        return oauthService.signInGoogle(request, response);
    }

    @PostMapping("/signin/naver")
    public ResponseEntity<ApiResponse<TokenResponse>> signInNaver (@RequestBody TokenRequest request,
                                                                   HttpServletResponse response){
        return oauthService.signInNaver(request, response);
    }
}
