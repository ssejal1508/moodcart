package com.moodcart.repository;

import com.moodcart.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Mood m JOIN m.products p WHERE m.id = :moodId")
    Page<Product> findByMoodId(@Param("moodId") Long moodId, Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY (p.likesCount + p.savesCount) DESC")
    Page<Product> findTrendingProducts(Pageable pageable);
}
