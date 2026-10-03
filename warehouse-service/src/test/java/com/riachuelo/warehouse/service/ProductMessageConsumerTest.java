package com.riachuelo.warehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.riachuelo.warehouse.dto.ProductCreatedEvent;
import com.riachuelo.warehouse.dto.ProductDeletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProductMessageConsumerTest {

    @Mock
    private StockService stockService;

    private ProductMessageConsumer messageConsumer;

    @BeforeEach
    void setUp() {
        messageConsumer = new ProductMessageConsumer(new ObjectMapper(), stockService);
    }

    @Test
    void consumesProductCreatedJsonAndRegistersStock() {
        String message = """
                {"productId":7,"name":"Tenis","description":"Tenis esportivo","price":199.90}
                """;

        messageConsumer.consumeProductCreated(message);

        ArgumentCaptor<ProductCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
        verify(stockService).registerProduct(eventCaptor.capture());
        assertThat(eventCaptor.getValue().productId()).isEqualTo(7L);
        assertThat(eventCaptor.getValue().name()).isEqualTo("Tenis");
    }

    @Test
    void consumesProductDeletedJsonAndRemovesStock() {
        messageConsumer.consumeProductDeleted("{\"productId\":7}");

        verify(stockService).removeProduct(new ProductDeletedEvent(7L));
    }

    @Test
    void rejectsDeletedMessageWithoutProductId() {
        assertThatThrownBy(() -> messageConsumer.consumeProductDeleted("{}"))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasMessageContaining("product ID");
        verifyNoInteractions(stockService);
    }

    @Test
    void rejectsMalformedMessage() {
        assertThatThrownBy(() -> messageConsumer.consumeProductCreated("not-json"))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasMessageContaining("Could not read");
    }

    @Test
    void rejectsMessageWithoutProductId() {
        String message = """
                {"name":"Tenis","description":"Tenis esportivo","price":199.90}
                """;

        assertThatThrownBy(() -> messageConsumer.consumeProductCreated(message))
                .isInstanceOf(AmqpRejectAndDontRequeueException.class)
                .hasMessageContaining("product ID");
        verifyNoInteractions(stockService);
    }
}
