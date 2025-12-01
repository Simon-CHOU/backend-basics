package com.simon.challenge_spring_transction_in_new_era.labs.selfcall;

import com.simon.challenge_spring_transction_in_new_era.labs.common.Entry;
import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class CallerService {
    private EntryRepository repo;
    private SeparateCalleeService callee;

    public CallerService() {}

    @org.springframework.beans.factory.annotation.Autowired
    public void setDeps(EntryRepository repo, SeparateCalleeService callee) {
        this.repo = repo;
        this.callee = callee;
    }

    @Transactional("transactionManager")
    public void outerAndFail() {
        repo.save(new Entry("OUTER_FIX", "x", Instant.now()));
        callee.inner();
        throw new IllegalStateException("fail");
    }
}
