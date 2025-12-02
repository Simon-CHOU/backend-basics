package com.example.mqlabs.lab02;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {
  private final JdbcTemplate jdbc;
  public KafkaConsumer(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @KafkaListener(topics = "lab02.user-status", groupId = "lab02-group")
  public void onMessage(String payload, org.apache.kafka.clients.consumer.ConsumerRecord<String,String> record) {
    var key = record.key();
    var inserted = jdbc.update("insert into kafka_inbox(message_key,processed_at) values(?,?) on conflict do nothing", key, java.time.Instant.now());
    if (inserted == 1) apply(payload);
  }
  private void apply(String payload) {
    var p = payload.split(":");
    jdbc.update("insert into kafka_notifications(user_id,count) values(?,1) on conflict (user_id) do update set count=kafka_notifications.count+1", p[0]);
  }
}
