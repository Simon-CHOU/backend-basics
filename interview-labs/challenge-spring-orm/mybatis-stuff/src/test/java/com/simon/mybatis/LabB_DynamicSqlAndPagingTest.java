package com.simon.mybatis;

import com.simon.mybatis.domain.User;
import com.simon.mybatis.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LabB_DynamicSqlAndPagingTest {
    @Autowired UserMapper users;

    @Test
    void AC_whereIfAndOffsetLimitWork() {
        for (int i = 0; i < 20; i++) {
            User u = new User();
            u.setEmail("u" + i + "@x.com");
            u.setName(i % 2 == 0 ? "Alice" : "Bob");
            users.insert(u);
        }

        List<User> page = users.selectPageByNameLike("Alice", 0, 5);
        assertEquals(5, page.size());
        assertTrue(page.stream().allMatch(x -> x.getName().equals("Alice")));
    }
}

