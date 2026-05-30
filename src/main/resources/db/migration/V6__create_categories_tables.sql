CREATE TABLE
    categories (
        id BIGSERIAL PRIMARY KEY,
        slug VARCHAR(120) NOT NULL UNIQUE,
        parent_id BIGINT REFERENCES,
        categories (id) ON DELETE SET NULL,
        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP NOT NULL,
        deleted_at TIMESTAMP
    );

CREATE TABLE
    category_translations (
        id BIGSERIAL PRIMARY KEY,
        category_id BIGINT NOT NULL REFERENCES,
        categories (id) ON DELETE CASCADE,
        language_code VARCHAR(2) NOT NULL, 
        name VARCHAR(100) NOT NULL,
        description VARCHAR(500),
        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP NOT NULL,
        deleted_at TIMESTAMP
    );

UNIQUE(category_id, language_code)
