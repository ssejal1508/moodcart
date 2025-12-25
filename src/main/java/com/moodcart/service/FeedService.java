package com.moodcart.service;

import com.moodcart.dto.FeedResponseDto;
import com.moodcart.dto.ProductDto;
import com.moodcart.dto.SaveProductDto;
import com.moodcart.entity.MoodBoard;
import com.moodcart.entity.Product;
import com.moodcart.entity.ProductInteraction;
import com.moodcart.entity.User;
import com.moodcart.exception.ResourceNotFoundException;
import com.moodcart.repository.MoodBoardRepository;
import com.moodcart.repository.ProductInteractionRepository;
import com.moodcart.repository.ProductRepository;
import com.moodcart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private MoodBoardRepository moodBoardRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductInteractionRepository interactionRepository;
    
    @Autowired
    private ProductService productService;
    
    // @Cacheable(value = "moodFeed", key = "#moodId + '_' + #page + '_' + #size")
    public FeedResponseDto getMoodFeed(Long moodId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByMoodId(moodId, pageable);
        
        User user = getCurrentUserOrNull();
        Long userId = user != null ? user.getId() : null;
        
        List<ProductDto> products = productPage.getContent().stream()
                .map(p -> productService.convertToDto(p, userId))
                .collect(Collectors.toList());
        
        return new FeedResponseDto(
                products,
                productPage.getNumber(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext()
        );
    }
    
    // @Cacheable(value = "trendingFeed", key = "#page + '_' + #size")
    public FeedResponseDto getTrendingFeed(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findTrendingProducts(pageable);
        
        User user = getCurrentUserOrNull();
        Long userId = user != null ? user.getId() : null;
        
        List<ProductDto> products = productPage.getContent().stream()
                .map(p -> productService.convertToDto(p, userId))
                .collect(Collectors.toList());
        
        return new FeedResponseDto(
                products,
                productPage.getNumber(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext()
        );
    }
    
    @Transactional
    public void saveProductToBoard(SaveProductDto dto) {
        User user = getCurrentUser();
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (!moodBoardRepository.existsByUserIdAndProductId(user.getId(), dto.getProductId())) {
            MoodBoard moodBoard = new MoodBoard();
            moodBoard.setUser(user);
            moodBoard.setProduct(product);
            moodBoardRepository.save(moodBoard);
            
            ProductInteraction interaction = new ProductInteraction();
            interaction.setUser(user);
            interaction.setProduct(product);
            interaction.setInteractionType(ProductInteraction.InteractionType.SAVE);
            interactionRepository.save(interaction);
            
            product.setSavesCount(product.getSavesCount() + 1);
            productRepository.save(product);
        }
    }
    
    public FeedResponseDto getUserMoodBoard(int page, int size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<MoodBoard> moodBoardPage = moodBoardRepository.findByUserId(user.getId(), pageable);
        
        List<ProductDto> products = moodBoardPage.getContent().stream()
                .map(mb -> productService.convertToDto(mb.getProduct(), user.getId()))
                .collect(Collectors.toList());
        
        return new FeedResponseDto(
                products,
                moodBoardPage.getNumber(),
                moodBoardPage.getTotalElements(),
                moodBoardPage.getTotalPages(),
                moodBoardPage.hasNext()
        );
    }
    
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    private User getCurrentUserOrNull() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
