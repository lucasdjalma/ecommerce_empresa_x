package com.riachuelo.warehouse.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange productExchange(
            @Value("${app.rabbitmq.product-exchange}") String exchangeName
    ) {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue stockQueue(@Value("${app.rabbitmq.stock-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding stockBinding(
            Queue stockQueue,
            TopicExchange productExchange,
            @Value("${app.rabbitmq.product-created-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(stockQueue).to(productExchange).with(routingKey);
    }

    @Bean
    public Queue stockDeletedQueue(@Value("${app.rabbitmq.stock-deleted-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding stockDeletedBinding(
            Queue stockDeletedQueue,
            TopicExchange productExchange,
            @Value("${app.rabbitmq.product-deleted-routing-key}") String routingKey
    ) {
        return BindingBuilder.bind(stockDeletedQueue).to(productExchange).with(routingKey);
    }
}