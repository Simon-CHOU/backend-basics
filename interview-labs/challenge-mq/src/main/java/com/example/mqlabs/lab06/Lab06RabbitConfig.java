package com.example.mqlabs.lab06;

import java.util.HashMap;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Lab06RabbitConfig {
  @Bean DirectExchange lab06Exchange() { return new DirectExchange("lab06.exchange"); }
  @Bean Queue lab06Main() { return new Queue("lab06.main", true); }
  @Bean Queue lab06Dlq() { return new Queue("lab06.dlq", true); }
  @Bean Binding bindMain(Queue lab06Main, DirectExchange lab06Exchange) { return BindingBuilder.bind(lab06Main).to(lab06Exchange).with("main"); }
  @Bean Binding bindDlq(Queue lab06Dlq, DirectExchange lab06Exchange) { return BindingBuilder.bind(lab06Dlq).to(lab06Exchange).with("dlq"); }
}
