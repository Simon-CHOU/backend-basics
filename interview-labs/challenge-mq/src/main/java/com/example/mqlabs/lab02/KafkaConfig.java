package com.example.mqlabs.lab02;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {
  @Bean NewTopic lab02Topic() { return new NewTopic("lab02.user-status", 3, (short)1); }
}
