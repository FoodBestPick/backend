  package org.example.backend.foodpick.infra.s3.configuration;

  import org.springframework.context.annotation.Bean;
  import org.springframework.context.annotation.Configuration;
  import org.springframework.security.config.annotation.web.builders.HttpSecurity;
  import org.springframework.security.web.SecurityFilterChain;

  @Configuration
  public class SecurityConfig {

      @Bean
      public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

          http
              .csrf(csrf -> csrf.disable())  // 🔹 회원가입 테스트 시 CSRF 비활성
              .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()   // 🔹 모든 요청 허용
            )
              .formLogin(form -> form.disable()) // 🔹 기본 로그인 폼 비활성화 (API 테스트용)
              .httpBasic(httpBasic -> httpBasic.disable()); // 🔹 Basic 인증도 꺼둠

          return http.build();
      }
  }
