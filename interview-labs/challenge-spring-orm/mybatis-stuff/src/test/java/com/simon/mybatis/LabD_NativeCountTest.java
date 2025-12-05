package com.simon.mybatis;

import com.simon.mybatis.domain.User;
import com.simon.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LabD_NativeCountTest {
    @Autowired UserMapper users;

    @Test
    void AC_nativeCountMatchesManualCount() {
        for (int i = 0; i < 3; i++) {
            User u = new User();
            u.setEmail("n" + i + "@x.com");
            u.setName("N" + i);
            users.insert(u);
        }
        long c = users.countUsers();
        assertEquals(3, c);
    }
}

