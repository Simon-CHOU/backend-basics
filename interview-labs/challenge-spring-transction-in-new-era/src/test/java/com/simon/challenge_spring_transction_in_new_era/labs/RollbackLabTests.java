package com.simon.challenge_spring_transction_in_new_era.labs;

import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import com.simon.challenge_spring_transction_in_new_era.labs.rollback.RollbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class RollbackLabTests {
    @Autowired RollbackService rollbackService;
    @Autowired EntryRepository repo;

    @BeforeEach
    void reset() { repo.deleteByType("CHK_DEF"); repo.deleteByType("CHK_ROLL"); }

    @Test
    void checkedDefaultDoesNotRollback() {
        assertThrows(Exception.class, () -> rollbackService.checkedDefault());
        assertThat(repo.countByType("CHK_DEF")).isEqualTo(1);
    }

    @Test
    void checkedRollbackRollsBack() {
        assertThrows(Exception.class, () -> rollbackService.checkedRollback());
        assertThat(repo.countByType("CHK_ROLL")).isZero();
    }
}
