package com.moodcart.repository;

import com.moodcart.entity.ProductInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductInteractionRepository extends JpaRepository<ProductInteraction, Long> {
    Optional<ProductInteraction> findByUserIdAndProductIdAndInteractionType(
        Long userId, Long productId, ProductInteraction.InteractionType interactionType
    );
    boolean existsByUserIdAndProductIdAndInteractionType(
        Long userId, Long productId, ProductInteraction.InteractionType interactionType
    );
}
