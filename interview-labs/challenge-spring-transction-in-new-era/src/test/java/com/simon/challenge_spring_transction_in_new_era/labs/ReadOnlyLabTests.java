package com.simon.challenge_spring_transction_in_new_era.labs;

import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import com.simon.challenge_spring_transction_in_new_era.labs.readonly.ReadOnlyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReadOnlyLabTests {
    @Autowired ReadOnlyService readOnlyService;
    @Autowired EntryRepository repo;

    @BeforeEach
    void reset() { repo.deleteByType("RO"); }

    @Test
    void readOnlyDoesNotPreventWrite() {
        readOnlyService.writeInsideReadOnly();
        assertThat(repo.countByType("RO")).isEqualTo(1);
    }
}
