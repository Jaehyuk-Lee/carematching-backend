package com.sesac.carematching.user;

import com.sesac.carematching.user.role.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final RoleService roleService;

    // 회원가입 로직
    public void registerUser(UserSignupDTO dto) {

        if(userRepository.findByUsername(dto.getUsername()).isPresent()){
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }

        // 비밀번호 확인
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 아이디 중복 확인
        if (!isUsernameAvailable(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 닉네임 중복 확인
        if (!isNicknameAvailable(dto.getNickname())) {
            throw new IllegalArgumentException("이미 있는 닉네임입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        // Users 객체 생성
        User user = new User(
            dto.getUsername(),
            encodedPassword, // 암호화된 비밀번호 저장
            dto.getNickname(),
            roleService.findRoleByName("ROLE_USER")
        );

        // 사용자 저장
        userRepository.save(user);
    }

    public User getUserInfo(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    // 아이디 중복 확인
    public boolean isUsernameAvailable(String username) {
        return userRepository.findByUsername(username).isEmpty();
    }

    // 닉네임 중복 확인
    public boolean isNicknameAvailable(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(String username){
        userRepository.deleteByUsername(username);
    }
}
