package com.example.travelplanning.data.local.category;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.travelplanning.data.model.category.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<Category> categories);

    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    List<Category> getAllCategories();
}