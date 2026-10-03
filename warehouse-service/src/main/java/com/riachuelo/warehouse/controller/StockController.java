package com.riachuelo.warehouse.controller;

import com.riachuelo.warehouse.dto.StockQuantityRequest;
import com.riachuelo.warehouse.dto.StockResponse;
import com.riachuelo.warehouse.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<List<StockResponse>> findAll() {
        return ResponseEntity.ok(stockService.findAll());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<StockResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.findByProductId(productId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<StockResponse> updateQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody StockQuantityRequest request
    ) {
        return ResponseEntity.ok(stockService.updateQuantity(productId, request.quantity()));
    }
}
