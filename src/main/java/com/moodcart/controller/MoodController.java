package com.moodcart.controller;

import com.moodcart.dto.FeedResponseDto;
import com.moodcart.dto.MoodDto;
import com.moodcart.service.FeedService;
import com.moodcart.service.MoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
