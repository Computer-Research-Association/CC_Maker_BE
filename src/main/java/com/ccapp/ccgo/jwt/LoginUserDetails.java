package com.ccapp.ccgo.jwt;

import com.ccapp.ccgo.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class LoginUserDetails implements UserDetails {

    private final User user;

    public LoginUserDetails(User user) {
        this.user = user;
    }

    // ✅ 기본 권한 비워둠 (나중에 ROLE_ 추가 가능)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    // ✅ 로그인 시 사용할 비밀번호
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // ✅ 로그인 시 사용할 식별자 (우린 이메일)
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // ✅ 계정 상태 기본 활성화 (true)
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
