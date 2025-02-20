package com.sesac.carematching.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/users")
public class UserController {

    @PostMapping("/signup")
    public ResponseEntity<Void> join(@RequestBody User user) {
        System.out.println("회원가입 컨트롤러 실행" + user);
        userService.joinUser(user);
        System.out.println("회원가입 완료");
        return ResponseEntity.ok().build();
    }
}
