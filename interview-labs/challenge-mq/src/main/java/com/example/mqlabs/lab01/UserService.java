package com.example.mqlabs.lab01;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final JdbcTemplate jdbc;
  public UserService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @Transactional(transactionManager = "transactionManager")
  public void changeStatusAndOutbox(String userId, String status) {
    jdbc.update("insert into users(id,status) values(?,?) on conflict (id) do update set status=excluded.status", userId, status);
    var payload = java.util.Base64.getEncoder().encodeToString(("{\"userId\":\""+userId+"\",\"status\":\""+status+"\"}").getBytes());
    jdbc.update("insert into outbox(aggregate_type,aggregate_id,type,payload,created_at,status) values(?,?,?,?,?,?)",
      "User", userId, "UserStatusChanged", payload, java.sql.Timestamp.from(java.time.Instant.now()), "NEW");
  }
}
