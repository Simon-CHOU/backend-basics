package com.example.mqlabs.lab06;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Lab06Controller {
  private final RabbitTemplate template;
  public Lab06Controller(RabbitTemplate template) { this.template = template; }
  @PostMapping("/lab06/send")
  public String send(@RequestParam("userId") String userId, @RequestParam("value") String value) {
    var m = org.springframework.amqp.core.MessageBuilder.withBody((userId+":"+value).getBytes()).setHeader("x-retry", 0).build();
    template.send("lab06.exchange", "main", m);
    return "OK";
  }
}
