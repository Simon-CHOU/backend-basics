package com.example.mqlabs.lab02;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaTxService {
  private final KafkaTemplate<String,String> template;
  public KafkaTxService(KafkaTemplate<String,String> template) { this.template = template; }
  public void changeStatusTransactional(String userId, String status) {
    template.executeInTransaction(t -> { t.send("lab02.user-status", userId, userId+":"+status); return true; });
  }
}
