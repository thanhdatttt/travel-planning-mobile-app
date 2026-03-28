package com.example.travelplanning.data.remote.category.dto.response;
import lombok.Data;
@Data
public class CategoryResponse {
    private Integer id;
    private String slug;
    private String nameEn;
    private String nameVi;
    private String icon;
    private Integer displayOrder;
}
