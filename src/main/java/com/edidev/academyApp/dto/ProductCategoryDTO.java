package com.edidev.academyApp.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private Integer sortOrder;
    private Integer productCount;
    private LocalDateTime createdAt;
}
