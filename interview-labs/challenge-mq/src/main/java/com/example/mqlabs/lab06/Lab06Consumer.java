package com.example.mqlabs.lab06;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Lab06Consumer {
  private final JdbcTemplate jdbc;
  private final RabbitTemplate template;
  public Lab06Consumer(JdbcTemplate jdbc, RabbitTemplate template) { this.jdbc = jdbc; this.template = template; }
  @RabbitListener(queues = "lab06.main")
  public void onMessage(org.springframework.amqp.core.Message message) {
    var payload = new String(message.getBody());
    var retry = (Integer) message.getMessageProperties().getHeaders().getOrDefault("x-retry", 0);
    if (payload.contains("FAIL") && retry < 3) {
      var m = org.springframework.amqp.core.MessageBuilder.fromMessage(message).setHeader("x-retry", retry+1).build();
      template.send("lab06.exchange", "main", m);
      return;
    }
    if (payload.contains("FAIL") && retry >= 3) {
      template.send("lab06.exchange", "dlq", message);
      return;
    }
    var p = payload.split(":");
    jdbc.update("insert into lab06_counts(user_id,count) values(?,1) on conflict (user_id) do update set count=lab06_counts.count+1", p[0]);
  }
}
