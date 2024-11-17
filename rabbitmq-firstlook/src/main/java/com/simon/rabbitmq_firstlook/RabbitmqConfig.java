package com.simon.rabbitmq_firstlook;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    @Bean
    Queue queue() {
        return new Queue(Constant.QUEUE_NAME);
    }

    @Bean
    TopicExchange topicQueue() {
        return new TopicExchange(Constant.EXCHANGE_NAME);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(topicQueue()).with(Constant.BIDING_KEY);
    }

    // 对象序列化之后才能通过网络传输

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
