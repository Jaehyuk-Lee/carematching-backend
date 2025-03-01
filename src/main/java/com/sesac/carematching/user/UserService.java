package com.sesac.carematching.user;

import com.sesac.carematching.user.dto.UserCertListDTO;
import com.sesac.carematching.user.dto.UserSignupDTO;
import com.sesac.carematching.user.dto.UserUpdateDTO;
import com.sesac.carematching.user.dto.UsernameDTO;
import com.sesac.carematching.user.role.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 연관된 데이터들은 JPA 엔티티에서 @OnDelete(action = OnDeleteAction.CASCADE) 설정으로 자동 삭제됨
        userRepository.delete(user);
    }

    @Transactional
    public void updateUser(String username, UserUpdateDTO dto) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
            user.setNickname(dto.getNickname());
        }

        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isEmpty()) {
            user.setPhone(dto.getPhoneNumber());
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        if (dto.getCertno() != null && !dto.getCertno().isEmpty()) {
            user.setCertno(dto.getCertno());
        }

        userRepository.save(user);
    }

    @Transactional
    public List<UserCertListDTO> getCertList() {
        return userRepository.findCert();
    }

    @Transactional
    public int updatePending(UsernameDTO usernameDTO, boolean pending) {
        User user = userRepository.findByUsername(usernameDTO.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setPending(pending);
        if (pending)
            user.setRole(roleService.findRoleByName("ROLE_USER"));
        else
            user.setRole(roleService.findRoleByName("ROLE_USER_CAREGIVER"));

        userRepository.save(user);

        return 0;
    }

}
