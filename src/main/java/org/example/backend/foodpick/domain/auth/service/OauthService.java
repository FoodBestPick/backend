package org.example.backend.foodpick.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.foodpick.domain.auth.dto.AuthInResponse;
import org.example.backend.foodpick.domain.auth.dto.SignInKakaoRequest;
import org.example.backend.foodpick.domain.auth.dto.SignInKakaoResponse;
import org.example.backend.foodpick.domain.auth.dto.TokenResponse;
import org.example.backend.foodpick.domain.auth.repository.AuthRepository;
import org.example.backend.foodpick.domain.user.dto.LoginUserResponse;
import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.example.backend.foodpick.domain.user.model.UserRole;
import org.example.backend.foodpick.domain.user.model.UserType;
import org.example.backend.foodpick.global.exception.CustomException;
import org.example.backend.foodpick.global.exception.ErrorException;
import org.example.backend.foodpick.global.jwt.JwtTokenProvider;
import org.example.backend.foodpick.global.util.ApiResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OauthService {

    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public ResponseEntity<ApiResponse<TokenResponse>> signInKakao(SignInKakaoRequest request) {

        String kakaoAccessToken = request.getToken();

        SignInKakaoResponse kakaoUser = getKakaoUserInfo(kakaoAccessToken);

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

        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);
        authRepository.save(user);

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

    public ResponseEntity<ApiResponse<TokenResponse>> signInGoogle(SignInKakaoRequest request) {

        String googleIdToken = request.getToken();

        SignInKakaoResponse googleUser = getGoogleUserInfo(googleIdToken);

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

        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);
        authRepository.save(user);

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

    public ResponseEntity<ApiResponse<TokenResponse>> signInNaver(SignInKakaoRequest request) {

        String naverAccessToken = request.getToken();

        SignInKakaoResponse naverUser = getNaverUserInfo(naverAccessToken);

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

        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        user.updateRefreshToken(refreshToken);
        authRepository.save(user);

        TokenResponse data = new TokenResponse(accessToken);

        LoginUserResponse userInfo = new LoginUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole() == UserRole.ADMIN
        );

        return ResponseEntity.ok(new AuthInResponse<>(200, "네이버 로그인 되었습니다.", data, userInfo));
    }

    private SignInKakaoResponse getKakaoUserInfo(String accessToken) {

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

        return new SignInKakaoResponse(email, nickname);
    }

    private SignInKakaoResponse getGoogleUserInfo(String idToken) {

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

        return new SignInKakaoResponse(email, nickname);
    }

    private SignInKakaoResponse getNaverUserInfo(String accessToken) {

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

        return new SignInKakaoResponse(email, nickname);
    }
}
