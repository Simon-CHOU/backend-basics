package com.simon.challenge_spring_transction_in_new_era.labs.requiresnew;

import com.simon.challenge_spring_transction_in_new_era.labs.common.Entry;
import com.simon.challenge_spring_transction_in_new_era.labs.common.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PaymentService {
    private final EntryRepository repo;
    private final AuditService auditService;

    public PaymentService(EntryRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    @Transactional("transactionManager")
    public void payAndFail(Long orderId) {
        repo.save(new Entry("PAYMENT", String.valueOf(orderId), Instant.now()));
        auditService.record("PAY:" + orderId);
        throw new IllegalStateException("fail after audit");
    }
}
