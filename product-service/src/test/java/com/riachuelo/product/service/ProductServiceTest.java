package com.riachuelo.product.service;

import com.riachuelo.product.dto.ProductCreatedEvent;
import com.riachuelo.product.dto.ProductDeletedEvent;
import com.riachuelo.product.dto.ProductRequest;
import com.riachuelo.product.dto.ProductResponse;
import com.riachuelo.product.entity.ProductEntity;
import com.riachuelo.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMessageProducer messageProducer;

    @InjectMocks
    private ProductService productService;

    @Test
    void createSavesProductAndPublishesCreatedEvent() {
        ProductRequest request = new ProductRequest("Camisa", "Camisa de algodao", new BigDecimal("79.90"));
        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request);

        assertThat(response.name()).isEqualTo("Camisa");
        assertThat(response.description()).isEqualTo("Camisa de algodao");
        assertThat(response.price()).isEqualByComparingTo("79.90");

        ArgumentCaptor<ProductCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
        verify(messageProducer).publishProductCreated(eventCaptor.capture());
        assertThat(eventCaptor.getValue().name()).isEqualTo("Camisa");
        assertThat(eventCaptor.getValue().price()).isEqualByComparingTo("79.90");
    }

    @Test
    void deleteRemovesProductAndPublishesDeletedEvent() {
        ProductEntity product = new ProductEntity("Camisa", "Camisa de algodao", new BigDecimal("79.90"));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        productService.delete(5L);

        verify(productRepository).delete(product);
        verify(messageProducer).publishProductDeleted(new ProductDeletedEvent(5L));
    }

    @Test
    void deleteDoesNotPublishWhenProductDoesNotExist() {
        when(productRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(5L))
                .isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(messageProducer);
    }

    @Test
    void findByIdThrowsNotFoundWhenProductDoesNotExist() {
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(10L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Product 10 was not found");
    }
}
