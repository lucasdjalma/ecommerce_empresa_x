package com.riachuelo.warehouse.dto;

import java.math.BigDecimal;

public record ProductCreatedEvent(Long productId, String name, String description, BigDecimal price) {
}
