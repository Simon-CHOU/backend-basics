package com.example.mqlabs.lab02;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "lab.kafka.enabled", havingValue = "true")
public class KafkaConfig {
  @Bean NewTopic lab02Topic() { return new NewTopic("lab02.user-status", 3, (short)1); }
}
