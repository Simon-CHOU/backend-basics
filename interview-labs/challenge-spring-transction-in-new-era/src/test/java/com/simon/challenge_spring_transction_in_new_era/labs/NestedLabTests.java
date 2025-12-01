package com.simon.challenge_spring_transction_in_new_era.labs;

import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import com.simon.challenge_spring_transction_in_new_era.labs.nested.BatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class NestedLabTests {
    @Autowired BatchService batchService;
    @Autowired EntryRepository repo;

    @BeforeEach
    void reset() { repo.deleteByType("ITEM"); }

    @Test
    void innerFailuresIsolatedBySavepoint() {
        batchService.importAll(List.of("a", "bad-1", "b", "bad-2"));
        assertThat(repo.countByType("ITEM")).isEqualTo(2);
    }

    @Test
    void outerRollbackClearsAllNested() {
        assertThrows(IllegalStateException.class, () -> batchService.importAndFail(List.of("a", "b")));
        assertThat(repo.countByType("ITEM")).isZero();
    }
}
