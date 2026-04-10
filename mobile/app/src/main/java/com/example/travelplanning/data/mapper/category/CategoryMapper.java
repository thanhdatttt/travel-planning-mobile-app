package com.example.travelplanning.data.mapper.category;

import com.example.travelplanning.data.model.category.Category;
import com.example.travelplanning.data.remote.category.dto.response.CategoryResponse;

import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {

    /**
     * Chuyển đổi từ DTO (Network) sang Domain Model (App)
     */
    public Category mapToDomain(CategoryResponse dto) {
        if (dto == null) return null;

        return Category.builder()
                .id(dto.getId())
                .slug(dto.getSlug())
                .nameEn(dto.getNameEn())
                .nameVi(dto.getNameVi())
                .icon(dto.getIcon())
                .displayOrder(dto.getDisplayOrder())
                .build();
    }

    /**
     * Chuyển đổi danh sách DTO sang danh sách Domain Model
     */
    public List<Category> mapToDomainList(List<CategoryResponse> dtoList) {
        if (dtoList == null) return new ArrayList<>();
        
        List<Category> domainList = new ArrayList<>();
        for (CategoryResponse dto : dtoList) {
            domainList.add(mapToDomain(dto));
        }
        return domainList;
    }

    /**
     * Chuyển đổi ngược lại từ Domain sang DTO (Nếu cần gửi data lên API)
     */
    public CategoryResponse mapToDto(Category domain) {
        if (domain == null) return null;

        CategoryResponse dto = new CategoryResponse();
        dto.setId(domain.getId());
        dto.setSlug(domain.getSlug());
        dto.setNameEn(domain.getNameEn());
        dto.setNameVi(domain.getNameVi());
        dto.setIcon(domain.getIcon());
        dto.setDisplayOrder(domain.getDisplayOrder());

        return dto;
    }
}