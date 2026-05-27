CREATE TABLE auth_providers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(20) NOT NULL, 
    provider_id VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),  
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_auth_providers_user_provider UNIQUE (user_id, provider)
);

CREATE INDEX idx_auth_providers_provider_id ON auth_providers(provider_id);