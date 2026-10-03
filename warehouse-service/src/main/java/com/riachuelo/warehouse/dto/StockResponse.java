package com.riachuelo.warehouse.dto;

import com.riachuelo.warehouse.entity.StockStatus;

public record StockResponse(Long productId, int quantity, StockStatus status) {
}
