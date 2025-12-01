package com.simon.challenge_spring_transction_in_new_era.labs.readonly;

import com.simon.challenge_spring_transction_in_new_era.labs.common.Entry;
import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ReadOnlyService {
    private final EntryRepository repo;

    public ReadOnlyService(EntryRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true, value = "transactionManager")
    public void writeInsideReadOnly() {
        repo.save(new Entry("RO", "r", Instant.now()));
    }
}
