package com.edidev.academyApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    /**
     * Sirve las imágenes subidas por el admin desde el directorio de uploads.
     * Las peticiones GET /api/catalog/uploads/** se resuelven contra el filesystem.
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String location = "file:" + uploadDir + "/products/";
        registry.addResourceHandler("/api/catalog/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
