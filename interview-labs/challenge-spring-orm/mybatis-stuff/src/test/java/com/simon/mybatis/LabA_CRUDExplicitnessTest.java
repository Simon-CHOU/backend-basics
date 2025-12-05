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
class LabA_CRUDExplicitnessTest {
    @Autowired UserMapper users;

    @Test
    void AC_updateRequiresExplicitMapperCall() {
        User u = new User();
        u.setEmail("a@b.com");
        u.setName("Alice");
        users.insert(u);

        User re = users.findById(u.getId());
        re.setName("Alice2");
        User re2 = users.findById(u.getId());
        assertEquals("Alice", re2.getName());

        int n = users.updateNameById(u.getId(), "Alice2");
        assertEquals(1, n);
        assertEquals("Alice2", users.findById(u.getId()).getName());
    }
}

