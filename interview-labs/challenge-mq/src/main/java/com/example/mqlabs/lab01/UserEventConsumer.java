package com.example.mqlabs.lab01;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {
  private final JdbcTemplate jdbc;
  public UserEventConsumer(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @RabbitListener(queues = "user.events")
  public void onMessage(org.springframework.amqp.core.Message message) {
    var id = Long.parseLong(message.getMessageProperties().getMessageId());
    var inserted = jdbc.update("insert into inbox(message_id,processed_at) values(?,?) on conflict do nothing", id, java.sql.Timestamp.from(java.time.Instant.now()));
    if (inserted == 1) apply(new String(message.getBody()));
  }
  private void apply(String payload) {
    var parts = payload.replace("\"", "").replace("{", "").replace("}", "").split(",");
    String userId = parts[0].split(":")[1];
    jdbc.update("insert into notifications(user_id,count) values(?,1) on conflict (user_id) do update set count=notifications.count+1", userId);
  }
}
