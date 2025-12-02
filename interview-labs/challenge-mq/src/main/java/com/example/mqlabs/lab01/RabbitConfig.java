package com.example.mqlabs.lab01;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  @Bean TopicExchange appExchange() { return new TopicExchange("app.events"); }
  @Bean Queue userQueue() { return new Queue("user.events", true); }
  @Bean Binding userBinding(Queue userQueue, TopicExchange appExchange) { return BindingBuilder.bind(userQueue).to(appExchange).with("User.*"); }
}
