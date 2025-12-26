package com.moodcart.controller;

import com.moodcart.dto.FeedResponseDto;
import com.moodcart.dto.MoodDto;
import com.moodcart.dto.ProductDto;
import com.moodcart.service.FeedService;
import com.moodcart.service.MoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/moods")
@Tag(name = "Moods", description = "Mood management endpoints")
public class MoodController {
    
    @Autowired
    private MoodService moodService;
    
    @Autowired
    private FeedService feedService;
    
    @GetMapping
    @Operation(summary = "Get all moods")
    public ResponseEntity<List<MoodDto>> getAllMoods() {
        return ResponseEntity.ok(moodService.getAllMoods());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get mood by ID")
    public ResponseEntity<MoodDto> getMoodById(@PathVariable Long id) {
        return ResponseEntity.ok(moodService.getMoodById(id));
    }
    
    @GetMapping("/{id}/feed")
    @Operation(summary = "Get product feed for a mood (Pinterest-style)")
    public ResponseEntity<FeedResponseDto> getMoodFeed(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(feedService.getMoodFeed(id, page, size));
    }

    @GetMapping("/vibe-feed")
    @Operation(summary = "Get product feed for an arbitrary vibe text")
    public ResponseEntity<FeedResponseDto> getVibeFeed(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            return ResponseEntity.ok(feedService.getVibeFeed(query, page, size));
        } catch (Exception ex) {
            // Fallback: never fail the request, just return an empty feed
            FeedResponseDto empty = new FeedResponseDto(
                    Collections.<ProductDto>emptyList(),
                    page,
                    0,
                    0,
                    false
            );
            return ResponseEntity.ok(empty);
        }
    }
}
