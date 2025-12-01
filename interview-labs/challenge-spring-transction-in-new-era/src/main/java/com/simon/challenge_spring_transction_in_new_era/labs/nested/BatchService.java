package com.simon.challenge_spring_transction_in_new_era.labs.nested;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BatchService {
    private final JdbcTemplate jdbc;
    private final NestedItemService nestedItemService;

    public BatchService(JdbcTemplate jdbc, NestedItemService nestedItemService) {
        this.jdbc = jdbc;
        this.nestedItemService = nestedItemService;
    }

    @Transactional(transactionManager = "dataSourceTransactionManager")
    public void importAll(List<String> names) {
        for (var name : names) {
            try {
                nestedItemService.importOne(name);
            } catch (RuntimeException ignored) {
            }
        }
    }

    @Transactional(transactionManager = "dataSourceTransactionManager")
    public void importAndFail(List<String> names) {
        importAll(names);
        throw new IllegalStateException("outer fail");
    }
}
