package com.example.travelplanning.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.databinding.AdminItemLocationBinding;

import java.util.List;

public class AdminLocationAdapter extends RecyclerView.Adapter<AdminLocationAdapter.LocationViewHolder> {
    private List<Location> locations;
    private final OnLocationOptionClickListener listener;

    public interface OnLocationOptionClickListener {
        void onOptionClick(View anchor, Location location);
    }

    public AdminLocationAdapter(List<Location> locations, OnLocationOptionClickListener listener) {
        this.locations = locations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminItemLocationBinding binding = AdminItemLocationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LocationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        holder.bind(locations.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return locations != null ? locations.size() : 0;
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        private final AdminItemLocationBinding binding;

        public LocationViewHolder(AdminItemLocationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Location location, OnLocationOptionClickListener listener) {
            binding.tvUsername.setText(location.getName());
            binding.tvRating.setText(String.valueOf(location.getAvgRating()));

            // Load Image
            Glide.with(binding.ivImage.getContext())
                    .load(location.getImageUrl())
                    .placeholder(R.drawable.suprised_car)
                    .into(binding.ivImage);

            // Handle Rating Balls/Stars logic
            setRatingBalls(location.getAvgRating());

            binding.btnOptions.setOnClickListener(v -> listener.onOptionClick(v, location));
        }

        private void setRatingBalls(double rating) {
            // Assuming you have a container or specific IDs for the 5 balls
            // We use an array for easy looping if you give them IDs like ivBall1, ivBall2...
            // Or if they are direct children of the rating layout:
            ViewGroup ratingContainer = (ViewGroup) binding.tvRating.getParent();

            // Logic: 0-5 stars
            for (int i = 1; i <= 5; i++) {
                // The rating images are usually after the TextView (index 0) and first ImageView (index 1)
                // It's safer to bind them directly in your XML or use a List
                ImageView ball = (ImageView) ratingContainer.getChildAt(i + 0); // Offset based on your XML

                if (rating >= i) {
                    ball.setImageResource(R.drawable.ic_rating_full);
                } else if (rating >= i - 0.5) {
                    ball.setImageResource(R.drawable.ic_rating_half);
                } else {
                    ball.setImageResource(R.drawable.ic_rating_empty);
                }
            }
        }
    }
}