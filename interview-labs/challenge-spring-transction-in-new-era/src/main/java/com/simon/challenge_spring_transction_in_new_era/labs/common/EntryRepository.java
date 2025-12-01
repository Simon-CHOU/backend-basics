package com.simon.challenge_spring_transction_in_new_era.labs.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EntryRepository extends JpaRepository<Entry, Long> {
    long countByType(String type);

    @Modifying
    @Transactional("transactionManager")
    @Query("delete from Entry e where e.type = ?1")
    void deleteByType(String type);
}
