package com.simon.challenge_spring_transction_in_new_era.labs;

import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import com.simon.challenge_spring_transction_in_new_era.labs.required.RequiredService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class RequiredLabTests {
    @Autowired RequiredService requiredService;
    @Autowired EntryRepository repo;

    @BeforeEach
    void reset() { repo.deleteByType("ORDER"); repo.deleteByType("LEDGER"); }

    @Test
    void okCommitsBoth() {
        requiredService.ok();
        assertThat(repo.countByType("ORDER")).isEqualTo(1);
        assertThat(repo.countByType("LEDGER")).isEqualTo(1);
    }

    @Test
    void failRollsBackBoth() {
        assertThrows(IllegalStateException.class, () -> requiredService.fail());
        assertThat(repo.countByType("ORDER")).isZero();
        assertThat(repo.countByType("LEDGER")).isZero();
    }
}
