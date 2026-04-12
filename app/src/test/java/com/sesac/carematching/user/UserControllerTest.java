package com.sesac.carematching.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.user.dto.UserInfoDTO;
import com.sesac.carematching.user.dto.UserSignupDTO;
import com.sesac.carematching.user.role.Role;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private TokenAuth tokenAuth;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        // MockMvc 및 ObjectMapper 초기화
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("회원 가입 요청을 받을 수 있다")
    void join_Success() throws Exception {
        // Given
        UserSignupDTO validUserSignupDTO = new UserSignupDTO();
        validUserSignupDTO.setUsername("testuser");
        validUserSignupDTO.setPassword("password123");
        validUserSignupDTO.setConfirmPassword("password123");
        validUserSignupDTO.setNickname("테스트유저");

        // When & Then
        mockMvc.perform(post("/api/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUserSignupDTO)))
            .andExpect(status().isOk());


        // registerUser(사용자 등록)는 반드시 한 번만 호출되어야 한다.
        verify(userService, times(1)).registerUser(any(UserSignupDTO.class));
    }

    @Test
    @DisplayName("관리자는 자격증 리스트를 볼 수 있다")
    void getCertList_Admin() throws Exception {
        // Given
        Role adminRole = new Role();
        adminRole.setRname("ROLE_ADMIN");
        User adminUser = new User();
        adminUser.setRole(adminRole);
        when(tokenAuth.extractUsernameFromToken(any())).thenReturn("user");
        when(userService.getUserInfo("user")).thenReturn(adminUser);

        // When
        ResultActions performed = mockMvc.perform(post("/api/user/admin/cert"));

        // Then
        performed.andExpect(status().isOk());
    }

    @Test
    @DisplayName("일반 회원은 자격증 리스트를 볼 수 없다")
    void getCertList_NonAdmin() throws Exception {
        // Given
        Role nonAdminRole = new Role();
        nonAdminRole.setRname("ROLE_USER");
        User nonAdminUser = new User();
        nonAdminUser.setRole(nonAdminRole);
        when(tokenAuth.extractUsernameFromToken(any())).thenReturn("user");
        when(userService.getUserInfo("user")).thenReturn(nonAdminUser);

        // When
        Exception exception = null;
        try {
            mockMvc.perform(post("/api/user/admin/cert"));
        } catch (ServletException e) {
            exception = e;
        }

        // Then
        assertInstanceOf(AdminAuthException.class, exception.getCause());
    }

    @Test
    @DisplayName("사용자 스스로의 정보를 조회할 수 있다")
    void getUser() throws Exception {
        // Given
        UserInfoDTO expectedResponse = new UserInfoDTO();
        expectedResponse.setNickname("testUser");
        expectedResponse.setPhoneNumber("01011112233");
        expectedResponse.setCertno("11223344");
        expectedResponse.setCreatedAt(LocalDateTime.now().toString());

        when(tokenAuth.extractUsernameFromToken(any())).thenReturn("user");
        when(userService.getUser("user")).thenReturn(expectedResponse);

        // When
        ResultActions performed = mockMvc.perform(get("/api/user/info"));

        // Then
        performed.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(jsonPath("$.nickname").value(expectedResponse.getNickname()))
            .andExpect(jsonPath("$.phoneNumber").value(expectedResponse.getPhoneNumber()))
            .andExpect(jsonPath("$.certno").value(expectedResponse.getCertno()))
            .andExpect(jsonPath("$.createdAt").value(expectedResponse.getCreatedAt()));
    }

    @Test
    @DisplayName("잘못된 경로 요청은 404 에러를 반환해야 한다")
    void requestInvalidURL() throws Exception {
        // When
        ResultActions result = mockMvc.perform(get("/api/user/invalid-path"));

        // Then
        result.andExpect(status().isNotFound());
    }

}
