package org.example.backend.foodpick.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    // 6자리 인증번호 생성
    public String generateAuthCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    // Redis 5분 저장
    public void saveAuthCode(String email, String code) {
        String key = "email:code:" + email;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
    }

    // Redis 인증 후 회원가입 가능한 메소드
    public void markVerified(String email) {
        String key = "email:verified:" + email;
        redisTemplate.opsForValue().set(key, "true", 30, TimeUnit.MINUTES);
    }

    // 인증번호 전송
    public void sendAuthCode(String toEmail, String authCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom("skaehgus113@naver.com");
        message.setSubject("[FoodPick] 이메일 인증번호 안내");
        message.setText("인증번호는 다음과 같습니다: " + authCode);

        mailSender.send(message);
    }

    // 인증번호 검증
    public void verifyCode(String email, String inputCode) {
        String key = "email:code:" + email;
        String savedCode = redisTemplate.opsForValue().get(key);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        if (savedCode == null) {
            throw new CustomException(ErrorException.EMAIL_CODE_NOT_FOUND);
        }

        if (ttl != null && ttl <= 0) {
            throw new CustomException(ErrorException.EMAIL_CODE_EXPIRED);
        }

        if (!savedCode.equals(inputCode)) {
            throw new CustomException(ErrorException.INVALID_CODE);
        }

        redisTemplate.delete(key);
    }

    public boolean isVerified(String email) {
        String key = "email:verified:" + email;
        String value = redisTemplate.opsForValue().get(key);
        return "true".equals(value);
    }

    public void clearVerified(String email) {
        String key = "email:verified:" + email;
        redisTemplate.delete(key);
    }
}