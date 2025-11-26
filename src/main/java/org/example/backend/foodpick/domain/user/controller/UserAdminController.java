package org.example.backend.foodpick.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.user.dto.SuspendeRequest;
import org.example.backend.foodpick.domain.user.dto.UserResponse;
import org.example.backend.foodpick.domain.user.dto.UserRoleRequest;
import org.example.backend.foodpick.domain.user.dto.WarningUpdateReqeust;
import org.example.backend.foodpick.domain.user.service.UserAdminService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserAdminService userAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUserAll(@RequestHeader("Authorization") String token){
        return userAdminService.getUserAll(token);
    }

    @PatchMapping("/{user_id}/warning")
    public ResponseEntity<ApiResponse<String>> warningUpdate(@RequestHeader("Authorization") String token,
                                                           @PathVariable("user_id") Long userId,
                                                           @RequestBody WarningUpdateReqeust request){
        return userAdminService.warningUpdate(token, userId, request);
    }

    @PatchMapping("/{user_id}/suspende")
    public ResponseEntity<ApiResponse<String>> userSuspende(@RequestHeader("Authorization") String token,
                                                            @PathVariable("user_id") Long userId,
                                                            @RequestBody SuspendeRequest request){
        return userAdminService.userSuspende(token, userId, request);
    }

    @PatchMapping("/{user_id}/role")
    public ResponseEntity<ApiResponse<String>> userRoleUpdate(@RequestHeader("Authorization") String token,
                                                              @PathVariable("user_id") Long userId,
                                                              @RequestBody UserRoleRequest request){
        return userAdminService.userRoleUpdate(token, userId, request);
    }
}
