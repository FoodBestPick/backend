package org.example.backend.foodpick.domain.user.principal;

import org.example.backend.foodpick.domain.user.model.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final UserEntity userEntity;
    private final Collection<GrantedAuthority> authorities;

    public CustomUserDetails(UserEntity userEntity, Collection<GrantedAuthority> authorities) {
        this.userEntity = userEntity;
        this.authorities = authorities;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    public String getUsername() {
        return userEntity.getNickname();
    }

    public String getEmail() {
        return userEntity.getEmail();
    }

    public Long getId() {
        return userEntity.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (true = 만료되지 않음)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (true = 잠금되지 않음)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 인증 정보 만료 여부 (true = 만료되지 않음)
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 (true = 활성화됨)
    }
}
