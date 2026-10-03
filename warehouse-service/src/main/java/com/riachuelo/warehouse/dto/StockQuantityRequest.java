package com.riachuelo.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockQuantityRequest(
        @NotNull @Min(0) Integer quantity
) {
}
