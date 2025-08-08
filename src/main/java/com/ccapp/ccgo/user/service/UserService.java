package com.ccapp.ccgo.user.service;

import com.ccapp.ccgo.user.dto.UserRequestDto;
import com.ccapp.ccgo.user.dto.UserResponseDto;
import com.ccapp.ccgo.user.dto.UserUpdateRequestDto;
import com.ccapp.ccgo.user.dto.PasswordChangeRequestDto;
import com.ccapp.ccgo.user.mapper.UserMapper;
import com.ccapp.ccgo.common.exception.CustomException;
import com.ccapp.ccgo.auth.jwt.JwtProvider;
import com.ccapp.ccgo.user.repository.UserRepository;
import com.ccapp.ccgo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        User user =userRepository.findById(id)
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

    // 7. 현재 로그인한 사용자 정보 조회
    public UserResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("로그인한 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        
        return UserMapper.toDto(user);
    }

    // 8. 현재 사용자 정보 부분 업데이트 (PATCH)
    public UserResponseDto updateCurrentUser(UserUpdateRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("로그인한 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        
        // 이메일 중복 체크 (다른 사용자가 같은 이메일을 사용하고 있는지)
        if (!email.equals(dto.getEmail())) {
            userRepository.findByEmail(dto.getEmail())
                    .ifPresent(existingUser -> {
                        throw new CustomException("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
                    });
        }
        
        // 변경된 필드만 업데이트
        UserMapper.updateEntityFromDto(user, dto);
        
        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    // 9. 현재 사용자 정보 전체 업데이트 (PUT)
    public UserResponseDto updateCurrentUserFull(UserUpdateRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("로그인한 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        
        // 이메일 중복 체크
        if (!email.equals(dto.getEmail())) {
            userRepository.findByEmail(dto.getEmail())
                    .ifPresent(existingUser -> {
                        throw new CustomException("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
                    });
        }
        
        // 모든 필드 업데이트
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setBirthdate(dto.getBirthdateAsLocalDate());
        user.setGender(dto.getGender());
        
        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    // 10. 비밀번호 변경
    public void changePassword(PasswordChangeRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("로그인한 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new CustomException("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        
        // 새 비밀번호로 변경
        String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(encodedNewPassword);
        
        userRepository.save(user);
    }
}
