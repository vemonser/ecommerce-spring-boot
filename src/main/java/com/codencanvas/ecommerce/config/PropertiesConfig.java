
package com.codencanvas.ecommerce.config;

import com.codencanvas.ecommerce.config.properties.CloudinaryProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
 


@Configuration
@EnableConfigurationProperties({
    CloudinaryProperties.class
})
public class PropertiesConfig {}
