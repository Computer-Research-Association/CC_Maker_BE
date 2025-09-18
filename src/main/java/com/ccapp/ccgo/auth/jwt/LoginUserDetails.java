package com.ccapp.ccgo.auth.jwt;

import com.ccapp.ccgo.team.entity.TeamMember;
import com.ccapp.ccgo.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class LoginUserDetails implements UserDetails {

    private final User user;
    private final List<TeamMember> teamMembers;

    public LoginUserDetails(User user, List<TeamMember> teamMembers) {
        this.user = user;
        this.teamMembers = teamMembers;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (teamMembers == null || teamMembers.isEmpty()) {
            return Collections.emptyList();
        }

        Set<SimpleGrantedAuthority> authorities = teamMembers.stream()
                .map(TeamMember::getRole) // Role enum 반환 가정
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        return authorities;
    }

    // 이하 기존 메서드 동일
    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

