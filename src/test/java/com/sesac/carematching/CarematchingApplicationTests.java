package com.sesac.carematching;

import com.sesac.carematching.caregiver.*;
import com.sesac.carematching.caregiver.dto.AddCaregiverRequest;
import com.sesac.carematching.caregiver.dto.UpdateCaregiverRequest;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import com.sesac.carematching.user.UserService;
import com.sesac.carematching.user.role.Role;
import com.sesac.carematching.user.role.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CarematchingApplicationTests {

    @Autowired
    private RoleService roleService;
    @Autowired
    private CaregiverService caregiverService;
    @Autowired
    private UserRepository userRepository;

	@Test
	void saveTest() {
        Role role = roleService.findRoleByName("ROLE_ADMIN");
        User user = new User();
        user.setPassword("비밀번호");
        user.setNickname("닉네임");
        user.setRole(role);
        user.setPending(false);
        user.setUsername("유저");
        userRepository.save(user);

        AddCaregiverRequest request = new AddCaregiverRequest();
        request.setLoc("지역");
        request.setSalary(10000);
        request.setStatus(Status.OPEN);
        request.setServNeeded("전문분야");
        request.setEmploymentType(EmploymentType.CONTRACT);
        request.setWorkDays("1111100");
        request.setWorkTime(WorkTime.FULLTIME);
        request.setWorkForm(WorkForm.LIVE_IN);
       // request.setUser(user);

//        Caregiver savedCaregiver = caregiverService.save(request);

        // 검증 구문
        assertNotNull(savedCaregiver, "저장된 caregiver는 null이 아니어야 합니다.");
        assertNotNull(savedCaregiver.getId(), "저장된 caregiver의 ID는 null이 아니어야 합니다.");
        assertEquals("지역", savedCaregiver.getLoc(), "지역 정보가 올바르게 저장되어야 합니다.");
        assertEquals(10000, savedCaregiver.getSalary(), "급여 정보가 올바르게 저장되어야 합니다.");
        assertEquals(Status.OPEN, savedCaregiver.getStatus(), "상태가 올바르게 저장되어야 합니다.");
        assertEquals("전문분야", savedCaregiver.getServNeeded(), "전문분야가 올바르게 저장되어야 합니다.");
        assertEquals(EmploymentType.CONTRACT, savedCaregiver.getEmploymentType(), "고용 형태가 올바르게 저장되어야 합니다.");
        assertEquals((byte) 0b1111100, savedCaregiver.getWorkDays(), "근무일 정보가 올바르게 저장되어야 합니다.");
        assertEquals(WorkTime.FULLTIME, savedCaregiver.getWorkTime(), "근무시간 정보가 올바르게 저장되어야 합니다.");
        assertEquals(WorkForm.LIVE_IN, savedCaregiver.getWorkForm(), "근무 형태가 올바르게 저장되어야 합니다.");
        assertNotNull(savedCaregiver.getUser(), "저장된 caregiver의 user 정보는 null이 아니어야 합니다.");
        assertEquals("유저", savedCaregiver.getUser().getUsername(), "user의 username이 올바르게 저장되어야 합니다.");
    }

    @Test
    void updateTest() {
        UpdateCaregiverRequest request = new UpdateCaregiverRequest();
        request.setLoc("지역2");
        request.setSalary(20000);
        request.setStatus(Status.CLOSE);
        request.setServNeeded("전문분야3");
        request.setEmploymentType(EmploymentType.CONTRACT);
        request.setWorkDays((byte) 0b0000000);
        request.setWorkTime(WorkTime.FULLTIME);
        request.setWorkForm(WorkForm.LIVE_IN);

        Caregiver updatedCaregiver = caregiverService.update(3, request);
        assertNotNull(updatedCaregiver, "업뎃된 caregiver는 null이 아니어야 합니다.");
        assertNotNull(updatedCaregiver.getId(), "업뎃된 caregiver의 ID는 null이 아니어야 합니다.");
        assertEquals("지역2", updatedCaregiver.getLoc(), "지역 정보가 올바르게 저장되어야 합니다.");
        assertEquals(20000, updatedCaregiver.getSalary(), "급여 정보가 올바르게 저장되어야 합니다.");
        assertEquals(Status.CLOSE, updatedCaregiver.getStatus(), "상태가 올바르게 저장되어야 합니다.");
        assertEquals("전문분야3", updatedCaregiver.getServNeeded(), "전문분야가 올바르게 저장되어야 합니다.");
        assertEquals((byte) 0b0000000, updatedCaregiver.getWorkDays(), "근무일 정보가 올바르게 저장되어야 합니다.");
        assertNotNull(updatedCaregiver.getUser(), "저장된 caregiver의 user 정보는 null이 아니어야 합니다.");
    }
}
