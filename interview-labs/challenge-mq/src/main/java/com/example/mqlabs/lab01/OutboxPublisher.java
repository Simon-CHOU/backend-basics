package com.example.mqlabs.lab01;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublisher {
  private final JdbcTemplate jdbc;
  public OutboxPublisher(JdbcTemplate jdbc) { this.jdbc = jdbc; }
  @Scheduled(fixedDelay = 2000)
  public void publishBatch() throws Exception {
    var list = jdbc.query("select id,aggregate_type,aggregate_id,type,payload from outbox where status='NEW' order by id limit 50", (rs,i)-> new OutboxMessage(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), java.time.Instant.now(), "NEW"));
    if (list.isEmpty()) return;
    var cf = new ConnectionFactory();
    cf.setHost("localhost");
    cf.setPort(5672);
    cf.setUsername("guest");
    cf.setPassword("guest");
    try (var conn = cf.newConnection(); var channel = conn.createChannel()) {
      channel.confirmSelect();
      for (var m : list) {
        var rk = m.aggregateType()+"."+m.aggregateId();
        var body = java.util.Base64.getDecoder().decode(m.payload());
        var props = new AMQP.BasicProperties.Builder().deliveryMode(2).messageId(String.valueOf(m.id())).build();
        channel.basicPublish("app.events", rk, props, body);
        jdbc.update("update outbox set status='SENT' where id=?", m.id());
      }
      channel.waitForConfirms();
    }
  }
}
