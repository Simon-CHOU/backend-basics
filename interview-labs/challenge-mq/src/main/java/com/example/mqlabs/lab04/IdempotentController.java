package com.example.mqlabs.lab04;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdempotentController {
  private final JdbcTemplate jdbc;
  public IdempotentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @PostMapping("/lab04/process")
  public String process(@RequestParam String messageId, @RequestParam String sku) {
    var inserted = jdbc.update("insert into lab04_inbox(message_id,processed_at) values(?,?) on conflict do nothing", messageId, java.time.Instant.now());
    if (inserted == 1) jdbc.update("insert into lab04_counts(sku,count) values(?,1) on conflict (sku) do update set count=lab04_counts.count+1", sku);
    return "OK";
  }
}
