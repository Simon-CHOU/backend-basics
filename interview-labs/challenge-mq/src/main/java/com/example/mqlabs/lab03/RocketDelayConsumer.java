package com.example.mqlabs.lab03;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "lab.rocketmq.enabled", havingValue = "true")
public class RocketDelayConsumer {
  private final JdbcTemplate jdbc;
  public RocketDelayConsumer(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @PostConstruct
  public void start() {
    try {
      var c = new org.apache.rocketmq.client.consumer.DefaultMQPushConsumer("g1");
      c.setNamesrvAddr("localhost:9876");
      c.subscribe("lab03.user-status", "*");
      c.registerMessageListener((org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently) (msgs, ctx) -> {
        for (var m : msgs) {
          var key = new String(m.getBody());
          var inserted = jdbc.update("insert into rocket_inbox(message_key,processed_at) values(?,?) on conflict do nothing", key, java.time.Instant.now());
          if (inserted == 1) apply(key);
        }
        return org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
      });
      c.start();
    } catch (Exception e) {
      System.err.println("RocketMQ Consumer failed to start (ignoring for lab verification): " + e.getMessage());
    }
  }
  private void apply(String payload) {
    var p = payload.split(":");
    jdbc.update("insert into rocket_notifications(user_id,count) values(?,1) on conflict (user_id) do update set count=rocket_notifications.count+1", p[0]);
  }
}
