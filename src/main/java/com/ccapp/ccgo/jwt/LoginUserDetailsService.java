package com.ccapp.ccgo.jwt;

import com.ccapp.ccgo.User;
import com.ccapp.ccgo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public LoginUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ 이메일로 유저 조회 → LoginUserDetails 로 감싸서 리턴
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        return new LoginUserDetails(user);
    }
}
