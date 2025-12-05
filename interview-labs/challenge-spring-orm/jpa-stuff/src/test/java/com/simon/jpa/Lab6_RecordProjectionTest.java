package com.simon.jpa;

import com.simon.jpa.domain.User;
import com.simon.jpa.dto.UserSummary;
import com.simon.jpa.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Lab6_RecordProjectionTest {
    @Autowired UserRepository repo;

    @Test
    void AC_recordProjectionConstructorExpressionReturnsData() {
        User u = new User();
        u.setEmail("p@x.com");
        u.setName("P");
        repo.save(u);

        List<UserSummary> summaries = repo.findAllSummaries();
        assertFalse(summaries.isEmpty());
        UserSummary s = summaries.get(0);
        assertNotNull(s.id());
        assertNotNull(s.email());
        assertNotNull(s.name());
    }
}

