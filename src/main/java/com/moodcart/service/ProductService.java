package com.moodcart.service;

import com.moodcart.dto.ProductDto;
import com.moodcart.dto.ProductRefDto;
import com.moodcart.entity.Mood;
import com.moodcart.entity.Product;
import com.moodcart.entity.ProductInteraction;
import com.moodcart.entity.User;
import com.moodcart.exception.ResourceNotFoundException;
import com.moodcart.repository.MoodRepository;
import com.moodcart.repository.ProductInteractionRepository;
import com.moodcart.repository.ProductRepository;
import com.moodcart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MoodRepository moodRepository;
    
    @Autowired
    private ProductInteractionRepository interactionRepository;
    
    public ProductDto createProduct(ProductDto productDto, Long moodId) {
        Mood mood = moodRepository.findById(moodId)
                .orElseThrow(() -> new ResourceNotFoundException("Mood not found"));
        
        Product product = new Product();
        product.setTitle(productDto.getTitle());
        product.setDescription(productDto.getDescription());
        product.setImageUrl(productDto.getImageUrl());
        product.setPrice(productDto.getPrice());
        product.setAffiliateUrl(productDto.getAffiliateUrl());
        product.setTags(productDto.getTags());
        
        product = productRepository.save(product);
        mood.getProducts().add(product);
        moodRepository.save(mood);
        
        return convertToDto(product, null);
    }
    
    @Transactional
    public void likeProduct(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (!interactionRepository.existsByUserIdAndProductIdAndInteractionType(
                user.getId(), productId, ProductInteraction.InteractionType.LIKE)) {
            
            ProductInteraction interaction = new ProductInteraction();
            interaction.setUser(user);
            interaction.setProduct(product);
            interaction.setInteractionType(ProductInteraction.InteractionType.LIKE);
            interactionRepository.save(interaction);
            
            product.setLikesCount(product.getLikesCount() + 1);
            productRepository.save(product);
        }
    }

    @Transactional
    public void likeProduct(ProductRefDto dto) {
        User user = getCurrentUser();
        Product product = resolveOrUpsertProduct(dto);
        likeProductInternal(user, product);
    }
    
    @Transactional
    public void unlikeProduct(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        interactionRepository.findByUserIdAndProductIdAndInteractionType(
                user.getId(), productId, ProductInteraction.InteractionType.LIKE
        ).ifPresent(interaction -> {
            interactionRepository.delete(interaction);
            product.setLikesCount(Math.max(0, product.getLikesCount() - 1));
            productRepository.save(product);
        });
    }

    @Transactional
    public void unlikeProduct(ProductRefDto dto) {
        User user = getCurrentUser();
        Product product = resolveOrUpsertProduct(dto);
        unlikeProductInternal(user, product);
    }

    private void likeProductInternal(User user, Product product) {
        Long productId = product.getId();
        if (!interactionRepository.existsByUserIdAndProductIdAndInteractionType(
            user.getId(), productId, ProductInteraction.InteractionType.LIKE)) {
            ProductInteraction interaction = new ProductInteraction();
            interaction.setUser(user);
            interaction.setProduct(product);
            interaction.setInteractionType(ProductInteraction.InteractionType.LIKE);
            interactionRepository.save(interaction);

            product.setLikesCount((product.getLikesCount() == null ? 0L : product.getLikesCount()) + 1);
            productRepository.save(product);
        }
    }

    private void unlikeProductInternal(User user, Product product) {
        Long productId = product.getId();
        interactionRepository.findByUserIdAndProductIdAndInteractionType(
            user.getId(), productId, ProductInteraction.InteractionType.LIKE
        ).ifPresent(interaction -> {
            interactionRepository.delete(interaction);
            product.setLikesCount(Math.max(0, (product.getLikesCount() == null ? 0L : product.getLikesCount()) - 1));
            productRepository.save(product);
        });
    }

    private Product resolveOrUpsertProduct(ProductRefDto dto) {
        if (dto == null) {
            throw new ResourceNotFoundException("Product not found");
        }

        if (dto.getProductId() != null) {
            return productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        }

        String externalId = dto.getExternalId();
        if (externalId == null || externalId.isBlank()) {
            throw new ResourceNotFoundException("Product not found");
        }

        Optional<Product> existing = productRepository.findByExternalId(externalId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Product created = new Product();
        created.setExternalId(externalId);
        created.setTitle(dto.getTitle());
        created.setImageUrl(dto.getImageUrl());
        created.setPrice(dto.getPrice());
        created.setAffiliateUrl(dto.getAffiliateUrl());
        created.setTags(dto.getTags());
        created.setLikesCount(0L);
        created.setSavesCount(0L);
        return productRepository.save(created);
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    public ProductDto convertToDto(Product product, Long userId) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setExternalId(product.getExternalId());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        dto.setPrice(product.getPrice());
        dto.setAffiliateUrl(product.getAffiliateUrl());
        dto.setTags(product.getTags());
        dto.setLikesCount(product.getLikesCount());
        dto.setSavesCount(product.getSavesCount());
        dto.setCreatedAt(product.getCreatedAt());
        
        if (userId != null) {
            dto.setLiked(interactionRepository.existsByUserIdAndProductIdAndInteractionType(
                    userId, product.getId(), ProductInteraction.InteractionType.LIKE));
            dto.setSaved(interactionRepository.existsByUserIdAndProductIdAndInteractionType(
                    userId, product.getId(), ProductInteraction.InteractionType.SAVE));
        }
        
        return dto;
    }
}
