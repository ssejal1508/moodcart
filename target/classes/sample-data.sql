-- Insert admin user (password: admin123)
INSERT INTO users (email, password, username, role) VALUES
('admin@moodcart.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin', 'ADMIN');

-- Insert sample moods
INSERT INTO moods (name, description, color_palette, image_url) VALUES
('Soft Girl', 'Pastel pink vibes with cute and cozy aesthetics', '#FFB3BA,#FFDFBA,#FFFFBA', 'https://images.unsplash.com/photo-1522198734915-76c764a8454d?w=400'),
('Dark Academia', 'Vintage scholarly aesthetic with moody tones', '#4A4238,#8B7355,#A0826D', 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400'),
('Late Night Coding', 'Tech-focused essentials for productivity', '#1E1E1E,#569CD6,#4EC9B0', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400'),
('Anime Core', 'Japanese anime and manga inspired style', '#FF6B9D,#C44569,#5758BB', 'https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400'),
('Cottagecore', 'Rural countryside aesthetic with natural elements', '#C7CEDB,#B4A7D6,#FFE5D9', 'https://images.unsplash.com/photo-1490730141103-6cac27aaab94?w=400'),
('Y2K', 'Early 2000s nostalgia with bold colors', '#FF00FF,#00FFFF,#FFFF00', 'https://images.unsplash.com/photo-1556656793-08538906a9f8?w=400');

-- Insert sample products
INSERT INTO products (title, description, image_url, price, affiliate_url, tags, likes_count, saves_count) VALUES
-- Soft Girl products
('Pink Cloud Hoodie', 'Oversized pastel pink hoodie with cloud embroidery', 'https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=400', 45.99, 'https://example.com/product1', 'clothing,pink,cozy', 245, 189),
('Heart-Shaped Sunglasses', 'Cute pink heart-shaped sunglasses', 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=400', 19.99, 'https://example.com/product2', 'accessories,pink,cute', 567, 423),
('Plush Teddy Bear', 'Giant soft pink teddy bear', 'https://images.unsplash.com/photo-1551447981-6e9750e2e1f6?w=400', 34.99, 'https://example.com/product3', 'decor,plush,pink', 892, 634),

-- Dark Academia products
('Leather Journal', 'Vintage brown leather bound journal', 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400', 28.99, 'https://example.com/product4', 'stationery,vintage,brown', 1203, 891),
('Tweed Blazer', 'Classic brown tweed blazer', 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=400', 89.99, 'https://example.com/product5', 'clothing,vintage,academic', 734, 567),
('Antique Reading Lamp', 'Brass desk lamp with vintage bulb', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=400', 67.99, 'https://example.com/product6', 'lighting,vintage,brass', 456, 389),

-- Late Night Coding products
('Mechanical Keyboard', 'RGB mechanical keyboard with blue switches', 'https://images.unsplash.com/photo-1595225476474-87563907a212?w=400', 129.99, 'https://example.com/product7', 'tech,keyboard,rgb', 1567, 1234),
('Ergonomic Mouse', 'Vertical ergonomic gaming mouse', 'https://images.unsplash.com/photo-1527814050087-3793815479db?w=400', 49.99, 'https://example.com/product8', 'tech,mouse,ergonomic', 892, 723),
('Blue Light Glasses', 'Anti-blue light computer glasses', 'https://images.unsplash.com/photo-1574258495973-f010dfbb5371?w=400', 39.99, 'https://example.com/product9', 'accessories,glasses,tech', 678, 534),

-- Anime Core products
('Anime Poster Set', 'Set of 3 anime art posters', 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=400', 24.99, 'https://example.com/product10', 'decor,anime,posters', 1890, 1456),
('Kawaii Backpack', 'Cute anime-inspired backpack', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400', 54.99, 'https://example.com/product11', 'accessories,anime,kawaii', 1234, 987),
('Manga Shelf', 'White floating shelves for manga collection', 'https://images.unsplash.com/photo-1594620302200-9a762244a156?w=400', 45.99, 'https://example.com/product12', 'furniture,storage,white', 567, 456);

-- Link products to moods
INSERT INTO mood_products (mood_id, product_id) VALUES
-- Soft Girl
(1, 1), (1, 2), (1, 3),
-- Dark Academia
(2, 4), (2, 5), (2, 6),
-- Late Night Coding
(3, 7), (3, 8), (3, 9),
-- Anime Core
(4, 10), (4, 11), (4, 12);
