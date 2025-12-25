package com.moodcart.service;

import com.moodcart.dto.MoodDto;
import com.moodcart.entity.Mood;
import com.moodcart.exception.ResourceNotFoundException;
import com.moodcart.repository.MoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MoodService {
    
    @Autowired
    private MoodRepository moodRepository;
    
    public List<MoodDto> getAllMoods() {
        return moodRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public MoodDto getMoodById(Long id) {
        Mood mood = moodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mood not found"));
        return convertToDto(mood);
    }
    
    public MoodDto createMood(MoodDto moodDto) {
        Mood mood = new Mood();
        mood.setName(moodDto.getName());
        mood.setDescription(moodDto.getDescription());
        mood.setColorPalette(moodDto.getColorPalette());
        mood.setImageUrl(moodDto.getImageUrl());
        
        mood = moodRepository.save(mood);
        return convertToDto(mood);
    }
    
    private MoodDto convertToDto(Mood mood) {
        MoodDto dto = new MoodDto();
        dto.setId(mood.getId());
        dto.setName(mood.getName());
        dto.setDescription(mood.getDescription());
        dto.setColorPalette(mood.getColorPalette());
        dto.setImageUrl(mood.getImageUrl());
        return dto;
    }
}
