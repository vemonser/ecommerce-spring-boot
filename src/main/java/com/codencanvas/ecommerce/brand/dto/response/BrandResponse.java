package com.codencanvas.ecommerce.brand.dto.response;

import java.time.Instant;

public record BrandResponse(
                Long id,
                String slug,
                String logoUrl,
                String name,
                String description,
                Boolean isActive,
                Instant createdAt, 
                Instant updatedAt,
                Instant deletedAt
) {
}
