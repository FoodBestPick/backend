package org.example.backend.foodpick.domain.inquiry.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryAnswerRequest;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryListResponse;
import org.example.backend.foodpick.domain.inquiry.model.InquiryStatus;
import org.example.backend.foodpick.domain.inquiry.service.InquiryAdminService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/inquiry")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryAdminService inquiryAdminService;

    @PatchMapping("/{inquiry_id}/answer")
    public ResponseEntity<ApiResponse<String>> answerInquiry(
            @PathVariable("inquiry_id") Long inquiryId,
            @RequestBody InquiryAnswerRequest request,
            @RequestHeader("Authorization") String token
    ) {
        return inquiryAdminService.answerInquiry(inquiryId, request, token);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryListResponse>>> getInquiries(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) InquiryStatus status
    ) {
        return inquiryAdminService.getInquiries(token, status);
    }
}
