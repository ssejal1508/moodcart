package com.moodcart.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRefDto {
    private Long productId;

    /**
     * For products coming from external sources (e.g., eBay).
     */
    private String externalId;

    // Only required when creating/upserting an external product
    private String title;
    private String imageUrl;
    private BigDecimal price;
    private String affiliateUrl;
    private String tags;

    @AssertTrue(message = "Provide productId OR provide externalId with title, imageUrl, and price")
    public boolean isValid() {
        if (productId != null) {
            return true;
        }
        if (externalId == null || externalId.isBlank()) {
            return false;
        }
        if (title == null || title.isBlank()) {
            return false;
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }
        return price != null;
    }
}
