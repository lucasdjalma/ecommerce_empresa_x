package com.riachuelo.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riachuelo.product.dto.ProductCreatedEvent;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductMessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchangeName;
    private final String routingKey;

    public ProductMessageProducer(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${app.rabbitmq.product-exchange}") String exchangeName,
            @Value("${app.rabbitmq.product-created-routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public void publishProductCreated(ProductCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize the product-created event.", exception);
        } catch (AmqpException exception) {
            // Broker indisponivel: a transacao e desfeita e o cliente recebe 503 em vez de 500.
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Message broker is unavailable. Try again later.",
                    exception
            );
        }
    }
}
