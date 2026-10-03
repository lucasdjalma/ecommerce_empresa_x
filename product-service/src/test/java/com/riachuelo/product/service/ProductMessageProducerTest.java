package com.riachuelo.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riachuelo.product.dto.ProductCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductMessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ProductMessageProducer messageProducer;

    @BeforeEach
    void setUp() {
        messageProducer = new ProductMessageProducer(
                rabbitTemplate,
                new ObjectMapper(),
                "product.exchange",
                "product.created"
        );
    }

    @Test
    void publishesProductCreatedEventAsJson() throws Exception {
        ProductCreatedEvent event = new ProductCreatedEvent(
                12L,
                "Calca",
                "Calca jeans",
                new BigDecimal("129.90")
        );

        messageProducer.publishProductCreated(event);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
                eq("product.exchange"),
                eq("product.created"),
                messageCaptor.capture()
        );
        ProductCreatedEvent published = new ObjectMapper().readValue(
                messageCaptor.getValue(),
                ProductCreatedEvent.class
        );
        assertThat(published).isEqualTo(event);
    }
}
