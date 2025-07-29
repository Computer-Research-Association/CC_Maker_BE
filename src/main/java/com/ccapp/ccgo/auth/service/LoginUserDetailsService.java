package com.ccapp.ccgo.auth.service;

import com.ccapp.ccgo.auth.jwt.LoginUserDetails;
import com.ccapp.ccgo.team.entity.TeamMember;
import com.ccapp.ccgo.team.repository.TeamMemberRepository;
import com.ccapp.ccgo.user.entity.User;
import com.ccapp.ccgo.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    // teamMemberRepository도 생성자 인자로 추가
    public LoginUserDetailsService(UserRepository userRepository,
                                   TeamMemberRepository teamMemberRepository) {
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    // ✅ 이메일로 유저 조회 → LoginUserDetails 로 감싸서 리턴
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        List<TeamMember> teamMembers = teamMemberRepository.findAllByUserAndIsActiveTrue(user);

        return new LoginUserDetails(user, teamMembers);
    }
}
