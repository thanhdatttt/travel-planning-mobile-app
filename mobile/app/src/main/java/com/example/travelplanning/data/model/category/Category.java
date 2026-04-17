package com.example.travelplanning.data.model.category;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@Ignore})
@Builder
public class Category {
    @PrimaryKey
    @NonNull
    private Integer id;
    
    private String slug;
    private String nameEn;
    private String nameVi;
    private String icon;
    private Integer displayOrder;
}