package com.moodcart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FeedResponseDto {
    private List<ProductDto> products;
    private int currentPage;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;
}
