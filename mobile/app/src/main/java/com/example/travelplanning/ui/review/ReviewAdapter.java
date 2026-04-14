package com.example.travelplanning.ui.review;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelplanning.data.model.review.Review; // Bạn tạo package model.review
import com.example.travelplanning.databinding.ItemReviewBinding; // XML item_review.xml
import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews = new ArrayList<>();

    public void setReviews(List<Review> newList) {
        this.reviews = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewBinding binding = ItemReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.binding.tvReviewUser.setText(review.getUserName() != null ? review.getUserName() : "Anonymous");
        holder.binding.tvReviewTitle.setText(review.getTitle());
        holder.binding.tvReviewBody.setText(review.getBody());
        holder.binding.rbItemRating.setRating(review.getRating());
        holder.binding.tvReviewDate.setText(review.getCreatedAt()); // Nên format lại date
    }

    @Override
    public int getItemCount() { return reviews.size(); }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ItemReviewBinding binding;
        ReviewViewHolder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
