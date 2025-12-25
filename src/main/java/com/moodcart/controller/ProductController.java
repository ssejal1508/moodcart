package com.moodcart.controller;

import com.moodcart.dto.FeedResponseDto;
import com.moodcart.dto.SaveProductDto;
import com.moodcart.service.FeedService;
import com.moodcart.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product interaction endpoints")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private FeedService feedService;
    
    @GetMapping("/trending")
    @Operation(summary = "Get trending products (most liked + saved)")
    public ResponseEntity<FeedResponseDto> getTrendingProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(feedService.getTrendingFeed(page, size));
    }
    
    @PostMapping("/{id}/like")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Like a product")
    public ResponseEntity<Void> likeProduct(@PathVariable Long id) {
        productService.likeProduct(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}/like")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Unlike a product")
    public ResponseEntity<Void> unlikeProduct(@PathVariable Long id) {
        productService.unlikeProduct(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/save")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Save product to mood board")
    public ResponseEntity<Void> saveProduct(@Valid @RequestBody SaveProductDto dto) {
        feedService.saveProductToBoard(dto);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/saved")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get user's saved products (mood board)")
    public ResponseEntity<FeedResponseDto> getSavedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(feedService.getUserMoodBoard(page, size));
    }
}
