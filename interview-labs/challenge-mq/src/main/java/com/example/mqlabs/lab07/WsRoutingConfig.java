package com.example.mqlabs.lab07;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WsRoutingConfig {
  @Bean DirectExchange wsDirect() { return new DirectExchange("ws.direct"); }
  @Bean Queue wsDlq() { return new Queue("ws.dlq", true); }
  @Bean Binding wsDlqBind(Queue wsDlq, DirectExchange wsDirect) { return BindingBuilder.bind(wsDlq).to(wsDirect).with("dlq"); }
}
