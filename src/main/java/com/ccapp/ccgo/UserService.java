package com.ccapp.ccgo;

import com.ccapp.ccgo.dto.UserRequestDto;
import com.ccapp.ccgo.dto.UserResponseDto;
import com.ccapp.ccgo.dto.UserMapper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // 단순 암호화용 인코더
    }

    // 📌 1. 회원가입
    public UserResponseDto register(User dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = UserMapper.toEntity(dto, encodedPassword);
        userRepository.save(user);

        return UserMapper.toDto(user);
    }

    // 📌 2. 로그인
    public UserResponseDto login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("등록되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return UserMapper.toDto(user);
    }

    // 📌 3. 전체 사용자 조회
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📌 4. 사용자 상세 조회
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자가 없습니다."));
        return UserMapper.toDto(user);
    }

    // 📌 5. 사용자 정보 수정
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자가 없습니다."));

        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setName(dto.getName());
        user.setGender(dto.getGender());
        user.setBirthdate(dto.getBirthdate());

        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    // 📌 6. 사용자 삭제
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("삭제할 사용자가 존재하지 않습니다.");
        }
        userRepository.deleteById(id);
    }
}
