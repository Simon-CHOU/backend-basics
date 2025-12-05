package com.simon.jpa;

import com.simon.jpa.domain.User;
import com.simon.jpa.repo.UserRepository;
import com.simon.jpa.spec.UserSpecs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Lab4_QueriesAndPaginationTest {
    @Autowired UserRepository repo;

    @Test
    void AC_derivedQueryAndJPQLPagingAndSpecificationWork() {
        for (int i = 0; i < 30; i++) {
            User u = new User();
            u.setEmail("u" + i + "@x.com");
            u.setName(i % 2 == 0 ? "Alice" : "Bob");
            repo.save(u);
        }

        assertTrue(repo.findByEmail("u0@x.com").isPresent());

        Page<User> page = repo.searchByName("Alice", PageRequest.of(0, 5));
        assertEquals(5, page.getContent().size());

        Page<User> specPage = repo.findAll(UserSpecs.nameContains("Bo"), PageRequest.of(0, 3));
        assertEquals(3, specPage.getContent().size());
    }
}

