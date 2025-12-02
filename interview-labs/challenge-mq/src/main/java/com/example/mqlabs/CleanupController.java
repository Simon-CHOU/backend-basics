package com.example.mqlabs;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CleanupController {
  private final JdbcTemplate jdbc;
  public CleanupController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @PostMapping("/cleanup/all")
  public String cleanup() {
    jdbc.update("delete from inbox");
    jdbc.update("delete from outbox");
    jdbc.update("delete from users");
    jdbc.update("delete from notifications");
    jdbc.update("delete from kafka_inbox");
    jdbc.update("delete from kafka_notifications");
    jdbc.update("delete from rocket_inbox");
    jdbc.update("delete from rocket_notifications");
    jdbc.update("delete from lab04_inbox");
    jdbc.update("delete from lab04_counts");
    jdbc.update("delete from lab05_log");
    jdbc.update("delete from lab06_counts");
    jdbc.update("delete from lab07_ws");
    return "OK";
  }
}
