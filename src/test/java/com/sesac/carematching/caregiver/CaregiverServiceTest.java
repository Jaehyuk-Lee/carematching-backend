package com.sesac.carematching.caregiver;

import com.sesac.carematching.caregiver.dto.AddCaregiverDto;
import com.sesac.carematching.user.User;
import com.sesac.carematching.user.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@SpringBootTest
class CaregiverServiceTest {

    @Autowired
    private CaregiverService caregiverService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void testSave() {
        User user = userRepository.findById(3).orElse(new User());
//        User user = new User();
//        userRepository.save(user);

        AddCaregiverDto request = new AddCaregiverDto();
        request.setLoc("지역");
        request.setSalary(1000);
        request.setWorkDays("월화수목금");
        request.setStatus(Status.CLOSE);
        request.setEmploymentType(EmploymentType.CONTRACT);
        request.setWorkForm(WorkForm.LIVE_IN);
        request.setWorkTime(WorkTime.FULLTIME);
        String username = "이지윤";

        // When
        Caregiver savedCaregiver = caregiverService.save(username, request);

        // Then
//        assertNotNull(savedCaregiver.getId()); // 자동 생성된 ID 확인
//        assertEquals("지역", savedCaregiver.getLoc());
//        assertEquals(1000, savedCaregiver.getSalary());
//
//        // Repository를 통해 실제 데이터베이스에 저장되었는지 확인
//        Caregiver foundCaregiver = caregiverRepository.findById(savedCaregiver.getId()).orElse(null);
//        assertNotNull(foundCaregiver);
//        assertEquals("지역", foundCaregiver.getLoc());
//        assertEquals(1000, foundCaregiver.getSalary());
    }
}
