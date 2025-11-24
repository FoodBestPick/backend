package org.example.backend.foodpick.infra.s3.service;

import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public ResponseEntity<ApiResponse<List<String>>> uploadToS3(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(400, "파일이 비어있습니다.", null));
        }

        try {
            List<String> uploadedUrls = uploadFiles(files);
            return ResponseEntity.ok(new ApiResponse<>(200, "S3 업로드 성공", uploadedUrls));
        } catch (Exception e) {
            log.error("파일 업로드 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok(new ApiResponse<>(500, "파일 업로드에 실패했습니다.", null));
        }
    }

    private List<String> uploadFiles(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String s3Key = generateS3Key(file);
                String url = uploadFile(file, s3Key);
                urls.add(url);
            } catch (Exception e) {
                log.error("파일 업로드 중 오류 발생: {}", e.getMessage(), e);
                throw new RuntimeException("파일 업로드에 실패했습니다: " + file.getOriginalFilename(), e);
            }
        }
        return urls;
    }

    private String uploadFile(MultipartFile file, String s3Key) throws Exception {
        Path tempFile = Files.createTempFile("upload-", ".tmp");
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, software.amazon.awssdk.core.sync.RequestBody.fromFile(tempFile));

            return getFileUrl(s3Key);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    public String uploadToS3Single(MultipartFile file) {
        try {
            String s3Key = generateS3Key(file);
            return uploadFile(file, s3Key);
        } catch (Exception e) {
            throw new CustomException(ErrorException.SERVER_ERROR);
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

    /**
     * 단일 S3 객체 삭제 (key 사용)
     */
    public void deleteByKey(String key) {
        try {
            DeleteObjectRequest req = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(req);
            log.debug("S3 delete success key={}", key);
        } catch (Exception e) {
            log.warn("S3 delete failed for key={} -> {}", key, e.getMessage());
            throw new RuntimeException("S3 삭제 실패: " + key, e);
        }
    }

    /**
     * 여러 객체를 한 번에 삭제 (keys)
     */
    public void deleteByKeys(List<String> keys) {
        if (keys == null || keys.isEmpty()) return;
        try {
            List<ObjectIdentifier> objs = keys.stream()
                    .map(k -> ObjectIdentifier.builder().key(k).build())
                    .collect(Collectors.toList());
            Delete delete = Delete.builder().objects(objs).build();
            DeleteObjectsRequest req = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(delete)
                    .build();
            s3Client.deleteObjects(req);
            log.debug("S3 batch delete success keys={}", keys.size());
        } catch (Exception e) {
            log.error("S3 일괄 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException("S3 일괄 삭제 실패", e);
        }
    }

    public void deleteByUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) return;
        List<String> keys = urls.stream()
                .map(this::keyFromUrl)
                .filter(k -> k != null && !k.isBlank())
                .collect(Collectors.toList());
        deleteByKeys(keys);
    }

    private String keyFromUrl(String url) {
        if (url == null) return null;
        String prefix1 = String.format("https://%s.s3.amazonaws.com/", bucketName);
        if (url.startsWith(prefix1)) return url.substring(prefix1.length());
        if (url.contains(".s3.amazonaws.com/")) {
            int idx = url.indexOf(".s3.amazonaws.com/");
            return url.substring(idx + ".s3.amazonaws.com/".length());
        }
        // fallback: if url contains bucketName/
        int idx = url.indexOf(bucketName + "/");
        if (idx >= 0) return url.substring(idx + bucketName.length() + 1);
        return null;
    }

    /**
     * ✅ URL로 S3 객체 삭제 (RestaurantService에서 사용)
     */
    public void deleteFromS3(String url) {
        if (url == null || url.isBlank()) {
            log.warn("삭제할 URL이 비어있습니다.");
            return;
        }
        String key = keyFromUrl(url);
        if (key == null || key.isBlank()) {
            log.warn("URL에서 S3 키를 추출할 수 없습니다: {}", url);
            return;
        }
        deleteByKey(key);
    }
}

