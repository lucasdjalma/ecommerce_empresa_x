package com.riachuelo.warehouse.service;

import com.riachuelo.warehouse.dto.ProductCreatedEvent;
import com.riachuelo.warehouse.dto.StockResponse;
import com.riachuelo.warehouse.entity.StockEntity;
import com.riachuelo.warehouse.repository.StockRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public void registerProduct(ProductCreatedEvent event) {
        if (!stockRepository.existsByProductId(event.productId())) {
            stockRepository.save(new StockEntity(event.productId()));
        }
    }

    public List<StockResponse> findAll() {
        return stockRepository.findAll(Sort.by(Sort.Direction.ASC, "productId"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StockResponse findByProductId(Long productId) {
        return toResponse(findStock(productId));
    }

    public StockResponse updateQuantity(Long productId, int quantity) {
        StockEntity stock = findStock(productId);
        stock.updateQuantity(quantity);
        return toResponse(stockRepository.save(stock));
    }

    private StockEntity findStock(Long productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stock for product " + productId + " was not found."
                ));
    }

    private StockResponse toResponse(StockEntity stock) {
        return new StockResponse(stock.getProductId(), stock.getQuantity(), stock.getStatus());
    }
}
