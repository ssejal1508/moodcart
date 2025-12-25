-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Moods table
CREATE TABLE IF NOT EXISTS moods (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    color_palette VARCHAR(255),
    image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(500) NOT NULL,
    price NUMERIC(10, 2),
    affiliate_url VARCHAR(500),
    tags TEXT,
    likes_count BIGINT DEFAULT 0,
    saves_count BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Mood-Product junction table (many-to-many)
CREATE TABLE IF NOT EXISTS mood_products (
    mood_id BIGINT NOT NULL REFERENCES moods(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (mood_id, product_id)
);

-- Mood boards (saved products per user)
CREATE TABLE IF NOT EXISTS mood_boards (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    saved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
);

-- Product interactions (likes/saves tracking)
CREATE TABLE IF NOT EXISTS product_interactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    interaction_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id, interaction_type)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_product_likes ON products(likes_count DESC);
CREATE INDEX IF NOT EXISTS idx_product_saves ON products(saves_count DESC);
CREATE INDEX IF NOT EXISTS idx_mood_boards_user ON mood_boards(user_id);
CREATE INDEX IF NOT EXISTS idx_product_interactions_user ON product_interactions(user_id);
CREATE INDEX IF NOT EXISTS idx_product_interactions_product ON product_interactions(product_id);
