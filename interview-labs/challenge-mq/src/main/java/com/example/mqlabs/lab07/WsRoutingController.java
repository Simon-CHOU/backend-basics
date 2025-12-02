package com.example.mqlabs.lab07;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WsRoutingController {
  private final DirectExchange exchange;
  private final org.springframework.amqp.core.AmqpAdmin admin;
  private final ConnectionFactory cf;
  private final RabbitTemplate template;
  private final JdbcTemplate jdbc;
  private final ConcurrentHashMap<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();
  public WsRoutingController(DirectExchange exchange, org.springframework.amqp.core.AmqpAdmin admin, ConnectionFactory cf, RabbitTemplate template, JdbcTemplate jdbc) { this.exchange = exchange; this.admin = admin; this.cf = cf; this.template = template; this.jdbc = jdbc; }
  @PostMapping("/lab07/register")
  public String register(@RequestParam String instanceId) {
    var q = new Queue("ws.instance."+instanceId, true);
    admin.declareQueue(q);
    Binding b = BindingBuilder.bind(q).to(exchange).with(instanceId);
    admin.declareBinding(b);
    var c = new SimpleMessageListenerContainer(cf);
    c.setQueueNames(q.getName());
    c.setMessageListener(m -> {
      var p = new String(m.getBody()).split(":");
      jdbc.update("insert into lab07_ws(user_id,instance_id,count) values(?,?,1) on conflict (user_id,instance_id) do update set count=lab07_ws.count+1", p[0], instanceId);
    });
    c.start();
    containers.put(instanceId, c);
    return "OK";
  }
  @PostMapping("/lab07/send")
  public String send(@RequestParam String instanceId, @RequestParam String userId) {
    template.convertAndSend(exchange.getName(), instanceId, userId+":MSG");
    return "OK";
  }
  @GetMapping("/lab07/stop")
  public String stop(@RequestParam String instanceId) {
    var c = containers.remove(instanceId);
    if (c != null) c.stop();
    return "OK";
  }
}
