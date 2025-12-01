package com.simon.challenge_spring_transction_in_new_era.labs.selfcall;

import com.simon.challenge_spring_transction_in_new_era.labs.common.Entry;
import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SelfCallService {
    private final EntryRepository repo;

    public SelfCallService(EntryRepository repo) { this.repo = repo; }

    @Transactional("transactionManager")
    public void outerAndFail() {
        repo.save(new Entry("OUTER", "x", Instant.now()));
        inner();
        throw new IllegalStateException("fail");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, value = "transactionManager")
    public void inner() {
        repo.save(new Entry("INNER", "y", Instant.now()));
    }
}
