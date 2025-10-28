package org.example.backend.foodpick.infra.s3.service;

import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public ApiResponse<List<String>> uploadToS3(List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                return ApiResponse.failure("파일이 비어있습니다.", 400);
            }

            List<String> uploadedUrls = uploadFiles(files);
            return new ApiResponse<>(200, true, "S3 업로드 성공", uploadedUrls);
            
        } catch (Exception e) {
            log.error("파일 업로드 처리 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.failure("파일 업로드에 실패했습니다.", 500);
        }
    }

    private List<String> uploadFiles(List<MultipartFile> files) {
        try {
            List<String> urls = new ArrayList<>();
            for (MultipartFile file : files) {
                String s3Key = generateS3Key(file);
                String url = uploadFile(file, s3Key);
                urls.add(url);
            }
            return urls;
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    private String uploadFile(MultipartFile file, String s3Key) throws Exception {
        Path tempFile = Files.createTempFile("upload-", ".tmp");
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Key)
                            .contentType(file.getContentType())
                            .build(),
                    tempFile
            );

            return getFileUrl(s3Key);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private String generateS3Key(MultipartFile file) {
        return String.format("uploads/%s/%s", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
            UUID.randomUUID().toString() + "_" + file.getOriginalFilename()
        );
    }

    private String getFileUrl(String s3Key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Key);
    }
}

