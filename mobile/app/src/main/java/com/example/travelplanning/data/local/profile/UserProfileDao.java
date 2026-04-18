package com.example.travelplanning.data.local.profile;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.travelplanning.data.model.profile.UserProfile;

@Dao
public interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProfile(UserProfile userProfile);

    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    UserProfile getProfileById(String userId);
}