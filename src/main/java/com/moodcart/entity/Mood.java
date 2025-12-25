package com.moodcart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "moods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mood {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "color_palette")
    private String colorPalette;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @ManyToMany
    @JoinTable(
        name = "mood_products",
        joinColumns = @JoinColumn(name = "mood_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
