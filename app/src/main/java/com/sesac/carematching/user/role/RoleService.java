package com.sesac.carematching.user.role;

import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public Role findRoleByName(String name) {
        return roleRepository.findByRname(name);
    }

    public void changeRoleToCaregiver (String username){
        User user = userRepository.findByUsername(username).orElseThrow(()->new IllegalArgumentException("User is null"));
        if (user.getRole().getRname() != "ROLE_ADMIN") {
            user.setRole(findRoleByName("ROLE_USER_CAREGIVER"));
        }
        userRepository.save(user);
    }
}
