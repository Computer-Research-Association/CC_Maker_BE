package com.ccapp.ccgo.service;

import com.ccapp.ccgo.dto.UserRequestDto;
import com.ccapp.ccgo.dto.UserResponseDto;
import com.ccapp.ccgo.dto.UserMapper;
import com.ccapp.ccgo.exception.CustomException;
import com.ccapp.ccgo.jwt.JwtProvider;
import com.ccapp.ccgo.repository.UserRepository;
import com.ccapp.ccgo.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    // 1. 회원가입
    public UserResponseDto register(UserRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new CustomException("이미 가입된 이메일입니다.", HttpStatus.CONFLICT);
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = UserMapper.toEntity(dto, encodedPassword);
        userRepository.save(user);

        return UserMapper.toDto(user);
    }

    // 2. 로그인: JWT 토큰 생성 반환
    public String loginAndGetToken(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        return jwtProvider.createAccessToken(authentication);
    }

    // 3. 전체 사용자 조회
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    // 4. 사용자 상세 조회
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("해당 ID의 사용자가 없습니다.", HttpStatus.NOT_FOUND));
        return UserMapper.toDto(user);
    }

    // 5. 사용자 정보 수정
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("해당 ID의 사용자가 없습니다.", HttpStatus.NOT_FOUND));

        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setName(dto.getName());
        user.setRole(dto.getRole());
        user.setGender(dto.getGender());
        user.setBirthdate(dto.getBirthdate());

        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    // 6. 사용자 삭제
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new CustomException("삭제할 사용자가 존재하지 않습니다.", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }
}
