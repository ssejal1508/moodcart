package com.moodcart.controller;

import com.moodcart.dto.MoodDto;
import com.moodcart.dto.ProductDto;
import com.moodcart.service.MoodService;
import com.moodcart.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin-only endpoints")
public class AdminController {
    
    @Autowired
    private MoodService moodService;
    
    @Autowired
    private ProductService productService;
    
    @PostMapping("/moods")
    @Operation(summary = "Create a new mood")
    public ResponseEntity<MoodDto> createMood(@Valid @RequestBody MoodDto moodDto) {
        return ResponseEntity.ok(moodService.createMood(moodDto));
    }
    
    @PostMapping("/moods/{moodId}/products")
    @Operation(summary = "Add product to a mood")
    public ResponseEntity<ProductDto> addProduct(
            @PathVariable Long moodId,
            @Valid @RequestBody ProductDto productDto) {
        return ResponseEntity.ok(productService.createProduct(productDto, moodId));
    }
}
