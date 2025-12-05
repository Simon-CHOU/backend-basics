package com.simon.jpa;

import com.simon.jpa.domain.User;
import com.simon.jpa.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Lab7_NativeQueryTest {
    @Autowired UserRepository repo;

    @Test
    void AC_nativeCountMatchesRepositoryCount() {
        for (int i = 0; i < 3; i++) {
            User u = new User();
            u.setEmail("n" + i + "@x.com");
            u.setName("N" + i);
            repo.save(u);
        }
        long n1 = repo.count();
        long n2 = repo.countUsersNative();
        assertEquals(n1, n2);
    }
}

