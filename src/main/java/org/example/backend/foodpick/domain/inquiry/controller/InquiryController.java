package org.example.backend.foodpick.domain.inquiry.controller;

import org.springframework.web.bind.annotation.RequestMapping;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryCreateRequest;
import org.example.backend.foodpick.domain.inquiry.dto.InquiryListResponse;
import org.example.backend.foodpick.domain.inquiry.service.InquiryService;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<InquiryListResponse>> createInquiry(
            @RequestPart(value = "data") InquiryCreateRequest request,
            @RequestPart(value = "file", required = false) List<MultipartFile> files,
            @RequestHeader("Authorization") String token
    ) {
        return inquiryService.createInquiry(request, files, token);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InquiryListResponse>>> getMyInquiries(
            @RequestHeader("Authorization") String token
    ) {
        return inquiryService.getMyInquiries(token);
    }

    @DeleteMapping("/delete/{inquiry_id}")
    public ResponseEntity<ApiResponse<String>> deleteInquiry(@PathVariable("inquiry_id") Long inquiryId,
                                                           @RequestHeader("Authorization") String token){
        return inquiryService.deleteInquiry(inquiryId, token);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<String>> deleteAllInquirys(@RequestHeader("Authorization") String token){
        return inquiryService.deleteAllInquirys(token);
    }

}
