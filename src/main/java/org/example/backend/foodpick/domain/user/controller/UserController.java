package org.example.backend.foodpick.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.auth.dto.ResetPasswordRequest;
import org.example.backend.foodpick.domain.user.dto.DeleteUserRequest;
import org.example.backend.foodpick.domain.user.dto.FcmTokenRequest;
import org.example.backend.foodpick.domain.user.dto.MyPagePasswordRequest;
import org.example.backend.foodpick.domain.user.dto.UserProfileResponse;
import org.example.backend.foodpick.domain.user.service.UserService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{user_id}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@RequestHeader("Authorization") String token,
                                                                        @PathVariable("user_id") Long userId) {
        return userService.getUserById(token, userId);
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUser(@RequestHeader("Authorization") String token) {
        return userService.getUser(token);
    }

    @PutMapping(value = "/profile", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> editUserProfile(
            @RequestHeader("Authorization") String token,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("nickname") String nickname,
            @RequestPart("stateMessage") String stateMessage) {

        return userService.editUserProfile(token, file, nickname, stateMessage);
    }

    @DeleteMapping("/profile/delete")
    public ResponseEntity<ApiResponse<String>> deleteUser(@RequestHeader("Authorization") String token,
                                                          @RequestBody DeleteUserRequest request){
        return userService.deleteUser(token,request);
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<String>> updateFcmToken(@RequestHeader("Authorization") String token,
                                                              @RequestBody FcmTokenRequest request){
        return userService.updateFcmToken(token, request);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestHeader("Authorization") String token,
                                                             @RequestBody MyPagePasswordRequest request){
        return userService.resetPassword(token, request);
    }
}
