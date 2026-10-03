package com.riachuelo.warehouse.service;

import com.riachuelo.warehouse.dto.ProductCreatedEvent;
import com.riachuelo.warehouse.dto.ProductDeletedEvent;
import com.riachuelo.warehouse.dto.StockResponse;
import com.riachuelo.warehouse.entity.StockEntity;
import com.riachuelo.warehouse.entity.StockStatus;
import com.riachuelo.warehouse.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void registerProductCreatesEmptyStockOnlyOnce() {
        ProductCreatedEvent event = new ProductCreatedEvent(
                7L,
                "Tenis",
                "Tenis esportivo",
                new BigDecimal("199.90")
        );
        when(stockRepository.existsByProductId(7L)).thenReturn(false);

        stockService.registerProduct(event);

        ArgumentCaptor<StockEntity> stockCaptor = ArgumentCaptor.forClass(StockEntity.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertThat(stockCaptor.getValue().getProductId()).isEqualTo(7L);
        assertThat(stockCaptor.getValue().getQuantity()).isZero();
        assertThat(stockCaptor.getValue().getStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }

    @Test
    void registerProductIgnoresDuplicateEvent() {
        ProductCreatedEvent event = new ProductCreatedEvent(7L, "Tenis", "Tenis esportivo", BigDecimal.ONE);
        when(stockRepository.existsByProductId(7L)).thenReturn(true);

        stockService.registerProduct(event);

        verify(stockRepository, never()).save(any(StockEntity.class));
    }

    @Test
    void removeProductDeletesExistingStock() {
        StockEntity stock = new StockEntity(7L);
        when(stockRepository.findByProductId(7L)).thenReturn(Optional.of(stock));

        stockService.removeProduct(new ProductDeletedEvent(7L));

        verify(stockRepository).delete(stock);
    }

    @Test
    void removeProductIgnoresUnknownProduct() {
        when(stockRepository.findByProductId(7L)).thenReturn(Optional.empty());

        stockService.removeProduct(new ProductDeletedEvent(7L));

        verify(stockRepository, never()).delete(any(StockEntity.class));
    }

    @Test
    void updateQuantityChangesStockStatus() {
        StockEntity stock = new StockEntity(7L);
        when(stockRepository.findByProductId(7L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);

        StockResponse response = stockService.updateQuantity(7L, 5);

        assertThat(response.quantity()).isEqualTo(5);
        assertThat(response.status()).isEqualTo(StockStatus.IN_STOCK);
    }

    @Test
    void stockCannotBeUpdatedToNegativeQuantity() {
        StockEntity stock = new StockEntity(7L);

        assertThatThrownBy(() -> stock.updateQuantity(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }
}
