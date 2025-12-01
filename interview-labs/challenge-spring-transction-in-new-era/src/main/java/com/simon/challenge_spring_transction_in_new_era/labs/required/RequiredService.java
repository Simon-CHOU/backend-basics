package com.simon.challenge_spring_transction_in_new_era.labs.required;

import com.simon.challenge_spring_transction_in_new_era.labs.common.Entry;
import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RequiredService {
    private final EntryRepository repo;

    public RequiredService(EntryRepository repo) { this.repo = repo; }

    @Transactional("transactionManager")
    public void ok() {
        repo.save(new Entry("ORDER", "ok-order", Instant.now()));
        repo.save(new Entry("LEDGER", "ok-ledger", Instant.now()));
    }

    @Transactional("transactionManager")
    public void fail() {
        repo.save(new Entry("ORDER", "fail-order", Instant.now()));
        repo.save(new Entry("LEDGER", "fail-ledger", Instant.now()));
        throw new IllegalStateException("boom");
    }
}
