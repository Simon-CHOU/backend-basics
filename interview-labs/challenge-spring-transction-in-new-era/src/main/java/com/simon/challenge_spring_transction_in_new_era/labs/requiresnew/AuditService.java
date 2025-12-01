package com.simon.challenge_spring_transction_in_new_era.labs.requiresnew;

import com.simon.challenge_spring_transction_in_new_era.labs.common.Entry;
import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuditService {
    private final EntryRepository repo;

    public AuditService(EntryRepository repo) { this.repo = repo; }

    @Transactional(propagation = Propagation.REQUIRES_NEW, value = "transactionManager")
    public void record(String payload) {
        repo.save(new Entry("AUDIT", payload, Instant.now()));
    }
}
