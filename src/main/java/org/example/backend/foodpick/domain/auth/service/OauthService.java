package org.example.backend.foodpick.domain.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.auth.dto.AuthInResponse;
import org.example.backend.foodpick.domain.auth.dto.TokenRequest;
import org.example.backend.foodpick.domain.auth.dto.SignInOauthResponse;
import org.example.backend.foodpick.domain.auth.dto.TokenResponse;
import org.example.backend.foodpick.domain.auth.repository.AuthRepository;
import org.example.backend.foodpick.domain.user.dto.LoginUserResponse;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.model.UserType;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenProvider;
import org.example.backend.foodpick.global.jwt.JwtTokenValidator;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.example.backend.foodpick.infra.redis.service.RedisDashboardService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OauthService {

    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenValidator jwtTokenValidator;
    private final RedisDashboardService redisDashboardService;

    public ResponseEntity<ApiResponse<TokenResponse>> signInKakao(TokenRequest request,
                                                                  HttpServletResponse response) {

        String kakaoAccessToken = request.getToken();

        SignInOauthResponse kakaoUser = getKakaoUserInfo(kakaoAccessToken);

        if (kakaoUser.getEmail() == null) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        UserEntity user = authRepository.findByEmail(kakaoUser.getEmail())
                .orElseGet(() ->
                        UserEntity.signInOauth(
                                kakaoUser.getEmail(),
                                kakaoUser.getNickname(),
                                UserType.KAKAO
                        )
                );

        authRepository.save(user);

        redisDashboardService.recordLogin(user.getId(), user.getRole());
        redisDashboardService.recordVisit(user.getId(), user.getRole());

        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24 * 14) // 14일
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());


        TokenResponse data = new TokenResponse(accessToken);

        LoginUserResponse userInfo = new LoginUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole() == UserRole.ADMIN
        );

        return ResponseEntity.ok(new AuthInResponse<>(200, "카카오 로그인 되었습니다.", data, userInfo)
        );
    }

    public ResponseEntity<ApiResponse<TokenResponse>> signInGoogle(TokenRequest request,
                                                                   HttpServletResponse response) {

        String googleIdToken = request.getToken();

        SignInOauthResponse googleUser = getGoogleUserInfo(googleIdToken);

        if (googleUser.getEmail() == null) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        UserEntity user = authRepository.findByEmail(googleUser.getEmail())
                .orElseGet(() ->
                        UserEntity.signInOauth(
                                googleUser.getEmail(),
                                googleUser.getNickname(),
                                UserType.GOOGLE
                        )
                );

        authRepository.save(user);

        redisDashboardService.recordLogin(user.getId(), user.getRole());
        redisDashboardService.recordVisit(user.getId(), user.getRole());

        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24 * 14) // 14일
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());


        TokenResponse data = new TokenResponse(accessToken);

        LoginUserResponse userInfo = new LoginUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole() == UserRole.ADMIN
        );

        return ResponseEntity.ok(new AuthInResponse<>(200, "구글 로그인 되었습니다.", data, userInfo)
        );
    }

    public ResponseEntity<ApiResponse<TokenResponse>> signInNaver(TokenRequest request,
                                                                  HttpServletResponse response) {

        String naverAccessToken = request.getToken();

        SignInOauthResponse naverUser = getNaverUserInfo(naverAccessToken);

        if (naverUser.getEmail() == null) {
            throw new CustomException(ErrorException.INVALID_EMAIL_FORMAT);
        }

        UserEntity user = authRepository.findByEmail(naverUser.getEmail())
                .orElseGet(() ->
                        UserEntity.signInOauth(
                                naverUser.getEmail(),
                                naverUser.getNickname(),
                                UserType.NAVER
                        )
                );

        authRepository.save(user);

        redisDashboardService.recordLogin(user.getId(), user.getRole());
        redisDashboardService.recordVisit(user.getId(), user.getRole());

        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24 * 14) // 14일
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        TokenResponse data = new TokenResponse(accessToken);

        LoginUserResponse userInfo = new LoginUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole() == UserRole.ADMIN
        );

        return ResponseEntity.ok(new AuthInResponse<>(200, "네이버 로그인 되었습니다.", data, userInfo));
    }

    private SignInOauthResponse getKakaoUserInfo(String accessToken) {

        String url = "https://kapi.kakao.com/v2/user/me";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map body = response.getBody();

        Map kakaoAccount = (Map) body.get("kakao_account");

        String email = (String) kakaoAccount.get("email");

        Map profile = (Map) kakaoAccount.get("profile");
        String nickname = profile != null ? (String) profile.get("nickname") : null;

        return new SignInOauthResponse(email, nickname);
    }

    private SignInOauthResponse getGoogleUserInfo(String idToken) {

        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Map.class
        );

        Map body = response.getBody();

        if (body == null) {
            throw new CustomException(ErrorException.SERVER_ERROR);
        }

        String email = (String) body.get("email");
        String nickname = (String) body.get("name");

        return new SignInOauthResponse(email, nickname);
    }

    private SignInOauthResponse getNaverUserInfo(String accessToken) {

        String url = "https://openapi.naver.com/v1/nid/me";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map body = response.getBody();

        if (body == null || body.get("response") == null) {
            throw new CustomException(ErrorException.SERVER_ERROR);
        }

        Map responseMap = (Map) body.get("response");

        String email = (String) responseMap.get("email");
        String nickname = (String) responseMap.get("nickname");

        return new SignInOauthResponse(email, nickname);
    }

}
