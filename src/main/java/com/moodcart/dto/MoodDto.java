package com.moodcart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MoodDto {
    private Long id;
    
    @NotBlank(message = "Mood name is required")
    private String name;
    
    private String description;
    private String colorPalette;
    private String imageUrl;
}
