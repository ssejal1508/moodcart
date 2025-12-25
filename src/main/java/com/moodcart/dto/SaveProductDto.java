package com.moodcart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveProductDto {
    @NotNull(message = "Product ID is required")
    private Long productId;
}
