package com.codencanvas.ecommerce.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.cloudinary")
public record CloudinaryProperties(
    String cloudName,
    String apiKey,
    String apiSecret
) {}
 