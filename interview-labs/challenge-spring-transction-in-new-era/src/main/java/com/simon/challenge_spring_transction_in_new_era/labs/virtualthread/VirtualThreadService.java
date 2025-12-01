package com.simon.challenge_spring_transction_in_new_era.labs.virtualthread;

import com.simon.challenge_spring_transction_in_new_era.labs.common.Entry;
import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class VirtualThreadService {
    private final EntryRepository repo;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public VirtualThreadService(EntryRepository repo) { this.repo = repo; }

    @Transactional("transactionManager")
    public void crossThreadAndFail() {
        var f = executor.submit(() -> repo.save(new Entry("VT_INNER", "i", Instant.now())));
        repo.save(new Entry("VT_OUTER", "o", Instant.now()));
        try { f.get(); } catch (Exception ignored) {}
        throw new IllegalStateException("outer fail");
    }
}
