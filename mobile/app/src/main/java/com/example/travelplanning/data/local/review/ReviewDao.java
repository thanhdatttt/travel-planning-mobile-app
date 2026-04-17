package com.example.travelplanning.data.local.review;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.travelplanning.data.model.review.RatingStat;
import com.example.travelplanning.data.model.review.Review;
import com.example.travelplanning.data.model.review.UserReview;

import java.util.List;

@Dao
public interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStats(List<RatingStat> stats);

    @Query("SELECT * FROM rating_stats WHERE locationId = :locationId")
    List<RatingStat> getStatsByLocation(String locationId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReviews(List<Review> reviews);

    @Query("DELETE FROM reviews WHERE locationId = :locationId")
    void clearReviewsByLocation(String locationId);

    @Query("SELECT * FROM reviews WHERE locationId = :locationId ORDER BY createdAt DESC")
    List<Review> getReviewsByLocation(String locationId);

    @Transaction 
    default void updateFirstPageReviews(String locationId, List<Review> newReviews) {
        clearReviewsByLocation(locationId);
        insertReviews(newReviews);
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMyReviews(List<UserReview> reviews);

    @Query("DELETE FROM my_reviews")
    void clearMyReviews();

    @Query("SELECT * FROM my_reviews")
    List<UserReview> getMyReviews();

    @Transaction
    default void updateFirstPageMyReviews(List<UserReview> newReviews) {
        clearMyReviews();
        insertMyReviews(newReviews);
    }
}