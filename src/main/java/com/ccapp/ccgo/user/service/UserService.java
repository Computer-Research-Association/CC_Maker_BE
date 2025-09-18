package com.ccapp.ccgo.user.service;

import com.ccapp.ccgo.user.dto.UserRequestDto;
import com.ccapp.ccgo.user.dto.UserResponseDto;
import com.ccapp.ccgo.user.dto.UserUpdateRequestDto;
import com.ccapp.ccgo.user.dto.PasswordChangeRequestDto;
import com.ccapp.ccgo.user.mapper.UserMapper;
import com.ccapp.ccgo.common.exception.CustomException;
import com.ccapp.ccgo.auth.jwt.JwtProvider;
import com.ccapp.ccgo.user.repository.UserRepository;
import com.ccapp.ccgo.user.repository.PrivacyAgreementRepository;
import com.ccapp.ccgo.user.entity.User;
import com.ccapp.ccgo.user.entity.PrivacyAgreement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PrivacyAgreementRepository privacyAgreementRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    // 1. 회원가입
    public UserResponseDto register(UserRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new CustomException("이미 가입된 이메일입니다.", HttpStatus.CONFLICT);
        }

        // 개인정보 동의 검증
        if (dto.isPrivacyAgreed()) {
            validatePrivacyAgreement(dto.getPrivacyAgreementVersion());
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = UserMapper.toEntity(dto, encodedPassword);
        userRepository.save(user);

        return UserMapper.toDto(user);
    }

    // 개인정보 동의서 버전 검증
    private void validatePrivacyAgreement(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new CustomException("개인정보 동의서 버전이 필요합니다.", HttpStatus.BAD_REQUEST);
        }
        
        // 해당 버전의 동의서가 존재하는지 확인
        if (!privacyAgreementRepository.existsByVersion(version)) {
            throw new CustomException("존재하지 않는 개인정보 동의서 버전입니다: " + version, HttpStatus.BAD_REQUEST);
        }
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
        
        UserMapper.updateEntityFromDto(user, dto);
        userRepository.save(user);
        
        return UserMapper.toDto(user);
    }

    // 9. 현재 사용자 정보 전체 업데이트 (PUT)
    public UserResponseDto updateCurrentUserFull(UserRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("로그인한 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        
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
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    // 11. 개인정보 동의서 조회 (현재 활성화된 버전)
    public PrivacyAgreement getCurrentPrivacyAgreement() {
        return privacyAgreementRepository.findByIsActiveTrue()
                .orElseThrow(() -> new CustomException("활성화된 개인정보 동의서가 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 12. 특정 버전의 개인정보 동의서 조회
    public PrivacyAgreement getPrivacyAgreementByVersion(String version) {
        return privacyAgreementRepository.findByVersion(version)
                .orElseThrow(() -> new CustomException("해당 버전의 개인정보 동의서가 없습니다: " + version, HttpStatus.NOT_FOUND));
    }
}
