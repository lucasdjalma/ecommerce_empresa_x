package com.riachuelo.warehouse.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riachuelo.warehouse.dto.ProductCreatedEvent;
import com.riachuelo.warehouse.dto.ProductDeletedEvent;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ProductMessageConsumer {

    private final ObjectMapper objectMapper;
    private final StockService stockService;

    public ProductMessageConsumer(ObjectMapper objectMapper, StockService stockService) {
        this.objectMapper = objectMapper;
        this.stockService = stockService;
    }

    @RabbitListener(queues = "${app.rabbitmq.stock-queue}")
    public void consumeProductCreated(String message) {
        ProductCreatedEvent event = read(message, ProductCreatedEvent.class, "product-created");
        if (event.productId() == null) {
            throw new AmqpRejectAndDontRequeueException("The product-created event must include a product ID.");
        }
        stockService.registerProduct(event);
    }

    @RabbitListener(queues = "${app.rabbitmq.stock-deleted-queue}")
    public void consumeProductDeleted(String message) {
        ProductDeletedEvent event = read(message, ProductDeletedEvent.class, "product-deleted");
        if (event.productId() == null) {
            throw new AmqpRejectAndDontRequeueException("The product-deleted event must include a product ID.");
        }
        stockService.removeProduct(event);
    }

    private <T> T read(String message, Class<T> type, String eventName) {
        try {
            return objectMapper.readValue(message, type);
        } catch (JsonProcessingException exception) {
            // Mensagem invalida nunca sera processada: descarta em vez de devolver a fila.
            throw new AmqpRejectAndDontRequeueException("Could not read the " + eventName + " event.", exception);
        }
    }
}
