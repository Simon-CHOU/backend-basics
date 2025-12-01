package com.simon.challenge_spring_transction_in_new_era.labs;

import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import com.simon.challenge_spring_transction_in_new_era.labs.selfcall.CallerService;
import com.simon.challenge_spring_transction_in_new_era.labs.selfcall.SelfCallService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class SelfCallLabTests {
    @Autowired SelfCallService selfCallService;
    @Autowired CallerService callerService;
    @Autowired EntryRepository repo;

    @BeforeEach
    void reset() { repo.deleteByType("OUTER"); repo.deleteByType("INNER"); repo.deleteByType("OUTER_FIX"); repo.deleteByType("INNER_FIX"); }

    @Test
    void selfInvocationCausesRollbackOfInner() {
        assertThrows(IllegalStateException.class, () -> selfCallService.outerAndFail());
        assertThat(repo.countByType("OUTER")).isZero();
        assertThat(repo.countByType("INNER")).isZero();
    }

    @Test
    void splitBeansEnableRequiresNew() {
        assertThrows(IllegalStateException.class, () -> callerService.outerAndFail());
        assertThat(repo.countByType("OUTER_FIX")).isZero();
        assertThat(repo.countByType("INNER_FIX")).isEqualTo(1);
    }
}
