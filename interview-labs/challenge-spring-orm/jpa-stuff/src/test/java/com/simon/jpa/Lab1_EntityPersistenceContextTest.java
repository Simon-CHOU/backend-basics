package com.simon.jpa;

import com.simon.jpa.domain.User;
import com.simon.jpa.repo.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Lab1_EntityPersistenceContextTest {

    @Autowired UserRepository repo;
    @PersistenceContext EntityManager em;


@Test
    @Transactional
    void AC_entityChangesAreFlushedWithoutExplicitSave() {
        User u = new User();
        u.setEmail("a@b.com");
        u.setName("Alice");
        repo.save(u);

        User managed = repo.findById(u.getId()).orElseThrow();
        managed.setName("Alice2");
        em.flush();
        em.clear();

        User reloaded = repo.findById(u.getId()).orElseThrow();
        assertEquals("Alice2", reloaded.getName());
        assertTrue(reloaded.getVersion() >= 1);
    }
}

