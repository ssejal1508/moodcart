package com.moodcart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDto {
    private Long id;

    // For external (e.g., eBay) products that are not yet persisted
    private String externalId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    @NotNull(message = "Price is required")
    private BigDecimal price;
    
    private String affiliateUrl;
    private String tags;
    private Long likesCount;
    private Long savesCount;
    private LocalDateTime createdAt;
    private boolean liked;
    private boolean saved;
}
