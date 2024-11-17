package com.simon.rabbitmq_firstlook;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducerController {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @PostMapping("/msg")
    public String sendMsg(@RequestBody Person person){
        System.out.println("send msg");
        rabbitTemplate.convertAndSend(Constant.EXCHANGE_NAME,Constant.ROUTING_KEY, person);
        return "success";
    }
}
