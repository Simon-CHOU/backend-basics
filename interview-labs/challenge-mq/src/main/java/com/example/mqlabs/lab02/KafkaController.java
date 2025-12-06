package com.example.mqlabs.lab02;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "lab.kafka.enabled", havingValue = "true")
public class KafkaController {
  private final KafkaTxService service;
  public KafkaController(KafkaTxService service) { this.service = service; }
  @PostMapping("/lab02/users/{id}/status")
  public String change(@PathVariable("id") String id, @RequestParam("value") String value) {
    service.changeStatusTransactional(id, value);
    return "OK";
  }
}
