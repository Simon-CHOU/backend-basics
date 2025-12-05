package com.simon.jpa;

import com.simon.jpa.domain.User;
import com.simon.jpa.repo.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Lab3_OptimisticLockTest {
    @Autowired UserRepository repo;
    @PersistenceContext EntityManager em;

    @Test
    @Transactional
    void AC_versionMismatchThrowsOptimisticLockException() {
        User u = new User();
        u.setEmail("lock@x.com");
        u.setName("L");
        repo.save(u);
        em.flush();

        User first = repo.findById(u.getId()).orElseThrow();
        first.setName("L1");
        em.flush();

        em.clear();

        User stale = new User();
        stale.setId(u.getId());
        stale.setEmail("lock@x.com");
        stale.setName("Stale");
        stale.setVersion(0);

        assertThrows(OptimisticLockException.class, () -> {
            em.merge(stale);
            em.flush();
        });
    }
}

