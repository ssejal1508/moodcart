package com.moodcart.service;

import com.moodcart.dto.FeedResponseDto;
import com.moodcart.dto.ProductDto;
import com.moodcart.dto.ProductRefDto;
import com.moodcart.dto.SaveProductDto;
import com.moodcart.entity.Mood;
import com.moodcart.entity.MoodBoard;
import com.moodcart.entity.Product;
import com.moodcart.entity.ProductInteraction;
import com.moodcart.entity.User;
import com.moodcart.exception.ResourceNotFoundException;
import com.moodcart.repository.MoodBoardRepository;
import com.moodcart.repository.MoodRepository;
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

    @Autowired
    private MoodRepository moodRepository;

    @Autowired
    private EbayClient ebayClient;
    
    public FeedResponseDto getMoodFeed(Long moodId, int page, int size) {
        Mood mood = moodRepository.findById(moodId)
            .orElseThrow(() -> new ResourceNotFoundException("Mood not found"));

        int sizeSafe = size <= 0 ? 20 : size;
        List<EbayClient.EbayItemSummary> ebayItems = ebayClient.searchItems(mood.getName(), page, sizeSafe);

        if (ebayItems != null && !ebayItems.isEmpty()) {
            List<ProductDto> products = ebayItems.stream()
                .map(this::fromEbayItem)
                .collect(Collectors.toList());
            return new FeedResponseDto(
                products,
                page,
                products.size(),
                products.isEmpty() ? 0 : page + 1,
                false
            );
        }

        // Fallback: DB mood products
        Pageable pageable = PageRequest.of(page, sizeSafe);
        Page<Product> dbPage = productRepository.findByMoodId(moodId, pageable);
        User user = getCurrentUserOrNull();
        Long userId = user != null ? user.getId() : null;
        List<ProductDto> products = dbPage.getContent().stream()
            .map(p -> productService.convertToDto(p, userId))
            .collect(Collectors.toList());

        return new FeedResponseDto(
            products,
            dbPage.getNumber(),
            dbPage.getTotalElements(),
            dbPage.getTotalPages(),
            dbPage.hasNext()
        );
    }
    
    public FeedResponseDto getTrendingFeed(int page, int size) {
        int sizeSafe = size <= 0 ? 20 : size;
        // Simple generic query to get interesting items; real app could tune this
        List<EbayClient.EbayItemSummary> ebayItems = ebayClient.searchItems("aesthetic", page, sizeSafe);

        if (ebayItems != null && !ebayItems.isEmpty()) {
            List<ProductDto> products = ebayItems.stream()
                .map(this::fromEbayItem)
                .collect(Collectors.toList());
            return new FeedResponseDto(
                products,
                page,
                products.size(),
                products.isEmpty() ? 0 : page + 1,
                false
            );
        }

        // Fallback: DB trending
        Pageable pageable = PageRequest.of(page, sizeSafe);
        Page<Product> dbPage = productRepository.findTrendingProducts(pageable);
        User user = getCurrentUserOrNull();
        Long userId = user != null ? user.getId() : null;
        List<ProductDto> products = dbPage.getContent().stream()
            .map(p -> productService.convertToDto(p, userId))
            .collect(Collectors.toList());

        return new FeedResponseDto(
            products,
            dbPage.getNumber(),
            dbPage.getTotalElements(),
            dbPage.getTotalPages(),
            dbPage.hasNext()
        );
    }

        public FeedResponseDto getVibeFeed(String query, int page, int size) {
        int sizeSafe = size <= 0 ? 20 : size;
        String effectiveQuery = (query == null || query.isBlank()) ? "aesthetic" : query;

        List<EbayClient.EbayItemSummary> ebayItems = ebayClient.searchItems(effectiveQuery, page, sizeSafe);

        if (ebayItems != null && !ebayItems.isEmpty()) {
            List<ProductDto> products = ebayItems.stream()
                .map(this::fromEbayItem)
                .collect(Collectors.toList());
            return new FeedResponseDto(
                products,
                page,
                products.size(),
                products.isEmpty() ? 0 : page + 1,
                false
            );
        }

        // Fallback: DB search
        Pageable pageable = PageRequest.of(page, sizeSafe);
        Page<Product> dbPage = productRepository.findByTitleContainingIgnoreCaseOrTagsContainingIgnoreCase(
            effectiveQuery,
            effectiveQuery,
            pageable
        );
        User user = getCurrentUserOrNull();
        Long userId = user != null ? user.getId() : null;
        List<ProductDto> products = dbPage.getContent().stream()
            .map(p -> productService.convertToDto(p, userId))
            .collect(Collectors.toList());

        return new FeedResponseDto(
            products,
            dbPage.getNumber(),
            dbPage.getTotalElements(),
            dbPage.getTotalPages(),
            dbPage.hasNext()
        );
        }
    
    @Transactional
    public void saveProductToBoard(SaveProductDto dto) {
        // Back-compat path: DB products
        if (dto == null || dto.getProductId() == null) {
            throw new ResourceNotFoundException("Product not found");
        }

        User user = getCurrentUser();
        Product product = productRepository.findById(dto.getProductId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        saveProductToBoardInternal(user, product);
    }

    @Transactional
    public void saveProductToBoard(ProductRefDto dto) {
        User user = getCurrentUser();
        Product product = resolveOrUpsertProduct(dto);
        saveProductToBoardInternal(user, product);
    }

    private void saveProductToBoardInternal(User user, Product product) {
        if (!moodBoardRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            MoodBoard moodBoard = new MoodBoard();
            moodBoard.setUser(user);
            moodBoard.setProduct(product);
            moodBoardRepository.save(moodBoard);

            ProductInteraction interaction = new ProductInteraction();
            interaction.setUser(user);
            interaction.setProduct(product);
            interaction.setInteractionType(ProductInteraction.InteractionType.SAVE);
            interactionRepository.save(interaction);

            product.setSavesCount((product.getSavesCount() == null ? 0L : product.getSavesCount()) + 1);
            productRepository.save(product);
        }
    }

    @Transactional
    public void unsaveProductFromBoard(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        unsaveInternal(user, product);
    }

    @Transactional
    public void unsaveProductFromBoard(ProductRefDto dto) {
        User user = getCurrentUser();

        Product product = null;
        if (dto != null && dto.getProductId() != null) {
            product = productRepository.findById(dto.getProductId()).orElse(null);
        } else if (dto != null && dto.getExternalId() != null && !dto.getExternalId().isBlank()) {
            product = productRepository.findByExternalId(dto.getExternalId()).orElse(null);
        }

        if (product == null) {
            // Nothing to unsave
            return;
        }

        unsaveInternal(user, product);
    }

    private void unsaveInternal(User user, Product product) {
        moodBoardRepository.findByUserIdAndProductId(user.getId(), product.getId()).ifPresent(mb -> {
            moodBoardRepository.delete(mb);
        });

        interactionRepository.findByUserIdAndProductIdAndInteractionType(
            user.getId(),
            product.getId(),
            ProductInteraction.InteractionType.SAVE
        ).ifPresent(interactionRepository::delete);

        product.setSavesCount(Math.max(0, (product.getSavesCount() == null ? 0L : product.getSavesCount()) - 1));
        productRepository.save(product);
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

    private ProductDto fromEbayItem(EbayClient.EbayItemSummary item) {
        ProductDto dto = new ProductDto();
        dto.setId(null); // Not persisted by default
        dto.setExternalId(item.itemId);
        dto.setTitle(item.title);
        dto.setDescription(item.shortDescription);
        dto.setImageUrl(item.getImageUrl() != null ? item.getImageUrl() : "");
        dto.setPrice(item.getPriceValue());
        dto.setAffiliateUrl(item.itemWebUrl);
        dto.setTags(item.getTagsString());

        // If we already have this item persisted, attach counts + per-user state
        Product persisted = (item.itemId == null || item.itemId.isBlank())
            ? null
            : productRepository.findByExternalId(item.itemId).orElse(null);

        if (persisted != null) {
            dto.setId(persisted.getId());
            dto.setLikesCount(persisted.getLikesCount() == null ? 0L : persisted.getLikesCount());
            dto.setSavesCount(persisted.getSavesCount() == null ? 0L : persisted.getSavesCount());

            User user = getCurrentUserOrNull();
            if (user != null) {
                dto.setLiked(interactionRepository.existsByUserIdAndProductIdAndInteractionType(
                    user.getId(), persisted.getId(), ProductInteraction.InteractionType.LIKE));
                dto.setSaved(interactionRepository.existsByUserIdAndProductIdAndInteractionType(
                    user.getId(), persisted.getId(), ProductInteraction.InteractionType.SAVE));
            } else {
                dto.setLiked(false);
                dto.setSaved(false);
            }
        } else {
            dto.setLikesCount(0L);
            dto.setSavesCount(0L);
            dto.setLiked(false);
            dto.setSaved(false);
        }
        return dto;
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

        Product existing = productRepository.findByExternalId(externalId).orElse(null);
        if (existing != null) {
            return existing;
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
}
