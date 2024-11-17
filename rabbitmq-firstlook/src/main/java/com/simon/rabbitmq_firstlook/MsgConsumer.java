package com.simon.rabbitmq_firstlook;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MsgConsumer {
    @RabbitListener(queues = Constant.QUEUE_NAME)
    public void handleMsg(Person person) {
        System.out.println("handle message" +person);
    }
}
