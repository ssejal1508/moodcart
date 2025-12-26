package com.moodcart.repository;

import com.moodcart.entity.MoodBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MoodBoardRepository extends JpaRepository<MoodBoard, Long> {
    Page<MoodBoard> findByUserId(Long userId, Pageable pageable);
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Optional<MoodBoard> findByUserIdAndProductId(Long userId, Long productId);
}
