package com.ccapp.ccgo;


import org.springframework.stereotype.Service;
import java.util.List;
import com.ccapp.ccgo.User;
import com.ccapp.ccgo.UserRepository;

@Service
public class UserService {

    // TodoRepo를 이 클래스 안에서 사용하기 위해 선언한 필드

    // 이렇게 final을 붙이면 무슨 뜻이냐면,
    // 이 변수(todoRepo)는 생성자에서 딱 한 번만 값이 들어갈 수 있고,
    // 그 이후에는 절대로 다른 객체로 바꿀 수 없다.
    private final UserRepository userRepository;

    // TodoService 클래스의 생성자
    // 클래스가 처음 만들어질 때 딱 한번 실행되는 특별한 메소드

    // 1. 객체가 만들어질 때 필요한 값을 전달받음
    // 2. 전달받은 값을 클래스 안의 필드에 저장

    // this.todoRepo → 클래스에 선언된 필드
    //todoRepo → 생성자 매개변수로 들어온 값
    //즉, "밖에서 받은 리포지토리를 내 필드에 저장한다"는 뜻
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //회원가입시
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    // findById()는 무조건 성공하지 않음
    // Optional<T>로 감싸서 "값이 없을 수 있어!" 라고 알려줌.

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자가 없습니다."));
    }

    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자가 없습니다."));

        user.setEmail(updatedUser.getEmail());
        user.setPassword(updatedUser.getPassword());

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("삭제할 사용자가 존재하지 않습니다.");
        }

        userRepository.deleteById(id);
    }

    //로그인 기능, password는 아직 미구현
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("등록되지 않은 이메일입니다."));
        return user;
    }

}