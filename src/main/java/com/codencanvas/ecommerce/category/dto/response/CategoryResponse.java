package com.codencanvas.ecommerce.category.dto.response;

import java.time.Instant;

public record CategoryResponse(
                Long id,
                String slug,
                Long parentId,
                String name,
                String description,
                Boolean isActive,
                Instant createdAt,
                Instant updatedAt,
                Instant deletedAt

) {
}
