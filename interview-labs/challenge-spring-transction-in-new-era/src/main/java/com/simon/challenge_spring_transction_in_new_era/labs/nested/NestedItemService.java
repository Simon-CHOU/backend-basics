package com.simon.challenge_spring_transction_in_new_era.labs.nested;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class NestedItemService {
    private final JdbcTemplate jdbc;

    public NestedItemService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(propagation = Propagation.NESTED, transactionManager = "dataSourceTransactionManager")
    public void importOne(String name) {
        jdbc.update("insert into entries(type, payload, created_at) values(?, ?, ?)", "ITEM", name, Instant.now());
        if (name.startsWith("bad")) throw new IllegalStateException("bad");
    }
}
