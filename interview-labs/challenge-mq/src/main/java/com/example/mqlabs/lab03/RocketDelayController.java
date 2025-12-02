package com.example.mqlabs.lab03;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RocketDelayController {
  @PostMapping("/lab03/delay")
  public String send(@RequestParam String userId, @RequestParam(defaultValue = "3") int level) throws Exception {
    var p = new org.apache.rocketmq.client.producer.DefaultMQProducer("p1");
    p.setNamesrvAddr("localhost:9876");
    p.start();
    var msg = new org.apache.rocketmq.common.message.Message("lab03.user-status", (userId+":DELAY").getBytes());
    msg.setDelayTimeLevel(level);
    p.send(msg);
    p.shutdown();
    return "OK";
  }
}
