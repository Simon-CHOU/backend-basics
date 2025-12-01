package com.simon.challenge_spring_transction_in_new_era.labs;

import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import com.simon.challenge_spring_transction_in_new_era.labs.virtualthread.VirtualThreadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class VirtualThreadLabTests {
    @Autowired VirtualThreadService svc;
    @Autowired EntryRepository repo;

    @BeforeEach
    void reset() { repo.deleteByType("VT_INNER"); repo.deleteByType("VT_OUTER"); }

    @Test
    void innerInNewThreadCommitsOuterRollsBack() {
        assertThrows(IllegalStateException.class, () -> svc.crossThreadAndFail());
        assertThat(repo.countByType("VT_OUTER")).isZero();
        assertThat(repo.countByType("VT_INNER")).isEqualTo(1);
    }
}
