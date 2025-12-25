package com.moodcart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_likes", columnList = "likes_count"),
    @Index(name = "idx_product_saves", columnList = "saves_count")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "affiliate_url")
    private String affiliateUrl;
    
    private String tags;
    
    @Column(name = "likes_count")
    private Long likesCount = 0L;
    
    @Column(name = "saves_count")
    private Long savesCount = 0L;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
