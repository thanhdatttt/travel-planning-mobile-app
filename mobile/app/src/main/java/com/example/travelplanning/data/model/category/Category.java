package com.example.travelplanning.data.model.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category {
    private Integer id;
    private String slug;
    private String nameEn;
    private String nameVi;
    private String icon;
    private Integer displayOrder;
}