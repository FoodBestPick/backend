package org.example.backend.foodpick.infra.s3.controller;

import org.example.backend.foodpick.infra.s3.service.S3Service;
import org.example.backend.foodpick.global.util.ApiResponse;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping(value = "/s3", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> uploadToS3(@RequestParam("files") List<MultipartFile> files) {
        return s3Service.uploadToS3(files);
    }
}
