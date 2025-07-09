package com.ccapp.ccgo.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BcryptTestController {

//    @GetMapping("/bcrypt-test")
//    public String test() {
//        String rawPw = "Test@1234";
//        String hash = "$2a$10$plQyO8OH8Sv5uBrm/fRO1OaXOCfcR5fZ0miWJ0OTZzzZEv6AfCMAO";
//
//        boolean matches = new BCryptPasswordEncoder().matches(rawPw, hash);
//        return "비밀번호 일치 여부: " + matches;
//    }
    @GetMapping("/bcrypt-test")
    public String generate() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPw = "Test@1234";
        String encodedPw = encoder.encode(rawPw);
        return encodedPw;
}
}
