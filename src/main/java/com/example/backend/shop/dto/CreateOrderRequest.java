package com.example.backend.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull List<CreateOrderItemRequest> items) {
    public record CreateOrderItemRequest(
            @NotNull Long productId,
            @Min(1) int quantity) {
    }
}