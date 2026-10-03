package com.riachuelo.product.dto;

import java.math.BigDecimal;

public record ProductCreatedEvent(Long productId, String name, String description, BigDecimal price) {
}
