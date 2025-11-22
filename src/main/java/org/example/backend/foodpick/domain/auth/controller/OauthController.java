package org.example.backend.foodpick.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.auth.dto.*;
import org.example.backend.foodpick.domain.auth.service.AuthService;
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
    public ResponseEntity<ApiResponse<TokenResponse>> signInKakao (@RequestBody SignInKakaoRequest request){
        return oauthService.signInKakao(request);
    }

    @PostMapping("/signin/google")
    public ResponseEntity<ApiResponse<TokenResponse>> signInGoogle (@RequestBody SignInKakaoRequest request){
        return oauthService.signInGoogle(request);
    }
}
