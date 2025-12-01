package com.simon.challenge_spring_transction_in_new_era.labs;

import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import com.simon.challenge_spring_transction_in_new_era.labs.requiresnew.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class RequiresNewLabTests {
    @Autowired PaymentService paymentService;
    @Autowired EntryRepository repo;

    @BeforeEach
    void reset() { repo.deleteByType("PAYMENT"); repo.deleteByType("AUDIT"); }

    @Test
    void innerCommitsOuterRollsBack() {
        assertThrows(IllegalStateException.class, () -> paymentService.payAndFail(1L));
        assertThat(repo.countByType("PAYMENT")).isZero();
        assertThat(repo.countByType("AUDIT")).isEqualTo(1);
    }
}
