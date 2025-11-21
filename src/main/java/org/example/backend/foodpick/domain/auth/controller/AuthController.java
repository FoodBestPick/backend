package org.example.backend.foodpick.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.auth.dto.*;
import org.example.backend.foodpick.domain.auth.service.AuthService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(@RequestBody AuthUpRequest request){
        return authService.signUp(request);
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<TokenResponse>> signIn(@RequestBody AuthInRequest request){
        return authService.signIn(request);
    }

    @PostMapping("/email-send")
    public ResponseEntity<ApiResponse<String>> emailSend(@RequestBody EmailSendRequest request){
        return authService.emailSend(request);
    }

    @PostMapping("/email-verify")
    public ResponseEntity<ApiResponse<String>> emailVerify(@RequestBody EmailVerifyRequest request){
        return authService.emailVerify(request);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request){
        return authService.resetPassword(request);
    }
}
