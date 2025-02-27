package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.AddCaregiverRequest;
import com.sesac.carematching.caregiver.dto.UpdateCaregiverRequest;
import com.sesac.carematching.caregiver.CaregiverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = CaregiverController.class,
    excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    }
)
public class CaregiverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CaregiverService caregiverService;

    @Autowired
    private UserRepository userRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CaregiverService caregiverService() {
            return Mockito.mock(CaregiverService.class);
        }
    }

    // JSON 문자열로 변환하는 유틸 메서드
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testFindCaregiver() throws Exception {
        Integer id = 3;
        // 테스트용 Caregiver 객체 생성 (필드에 맞게 수정)
        Caregiver caregiver = new Caregiver();
        caregiver.setId(id);
        caregiver.setRealName("real name");

        when(caregiverService.findById(id)).thenReturn(caregiver);

        mockMvc.perform(get("/api/caregivers/get/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.realName").value("real name"));
    }

    @Test
    public void testDeleteCaregiver() throws Exception {
        Integer id = 3;
        doNothing().when(caregiverService).delete(id);

        mockMvc.perform(delete("/api/caregivers/delete/{id}", id))
            .andExpect(status().isOk());
    }

    @Test
    public void testUpdateCaregiver() throws Exception {
        Integer id = 1;
        // 업데이트 요청 객체 생성 (필드에 맞게 수정)
        UpdateCaregiverRequest request = new UpdateCaregiverRequest();
        request.setRealName("Updated Name");

        // 업데이트된 Caregiver 객체 생성
        Caregiver updatedCaregiver = new Caregiver();
        updatedCaregiver.setId(id);
        updatedCaregiver.setRealName("Updated Name");

        when(caregiverService.update(eq(id), any(UpdateCaregiverRequest.class)))
            .thenReturn(updatedCaregiver);

        mockMvc.perform(put("/api/caregivers/update/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.realName").value("Updated Name"));
    }

    @Test
    public void testAddCaregiver() throws Exception {
        // 추가 요청 객체 생성 (필드에 맞게 수정)
        AddCaregiverRequest request = new AddCaregiverRequest();
        request.setRealName("New Caregiver");

        // 저장된 Caregiver 객체 생성 (예를 들어, id가 할당된 경우)
        Caregiver savedCaregiver = new Caregiver();
        savedCaregiver.setId(1);
        savedCaregiver.setRealName("New Caregiver");

        when(caregiverService.save(any(AddCaregiverRequest.class)))
            .thenReturn(savedCaregiver);

        mockMvc.perform(post("/api/caregivers/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.realName").value("New Caregiver"));
    }
}
