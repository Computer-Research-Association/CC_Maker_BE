package com.ccapp.ccgo.common.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BcryptTestController {

    @GetMapping("/bcrypt-test")
    public String test() {
        String rawPw = "Test@1234";
        String hash = "$2a$10$l.Y.cfT9oRlzsiH7fSBIWearf3bijLIDR4ZSsTCnb7IsMvl8NWlnW";

        boolean matches = new BCryptPasswordEncoder().matches(rawPw, hash);
        return "비밀번호 일치 여부: " + matches;
    }
    @GetMapping("/bcrypt-config")
    public String generate() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPw = "Test@1234";
        String encodedPw = encoder.encode(rawPw);
        return encodedPw;
    }
}
