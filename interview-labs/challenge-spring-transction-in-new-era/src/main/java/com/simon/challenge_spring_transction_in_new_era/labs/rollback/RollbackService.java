package com.simon.challenge_spring_transction_in_new_era.labs.rollback;

import com.simon.challenge_spring_transction_in_new_era.labs.common.Entry;
import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RollbackService {
    private final EntryRepository repo;

    public RollbackService(EntryRepository repo) { this.repo = repo; }

    @Transactional("transactionManager")
    public void checkedDefault() throws Exception {
        repo.save(new Entry("CHK_DEF", "a", Instant.now()));
        throw new Exception("x");
    }

    @Transactional(rollbackFor = Exception.class, value = "transactionManager")
    public void checkedRollback() throws Exception {
        repo.save(new Entry("CHK_ROLL", "b", Instant.now()));
        throw new Exception("y");
    }
}
