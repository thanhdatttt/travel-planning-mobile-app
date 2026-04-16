package com.example.travelplanning.ui.review;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelplanning.data.model.review.Review;
import com.example.travelplanning.data.model.review.UserReview;
import com.example.travelplanning.databinding.ItemMyReviewBinding;
import java.util.ArrayList;
import java.util.List;

public class MyReviewAdapter extends RecyclerView.Adapter<MyReviewAdapter.MyReviewViewHolder> {
    private List<UserReview> list = new ArrayList<>();
    private final OnReviewClickListener listener;

    public interface OnReviewClickListener {
        void onLocationClick(String locationId);
    }

    public MyReviewAdapter(OnReviewClickListener listener) {
        this.listener = listener;
    }

    public void setReviews(List<UserReview> reviewList){
        this.list = reviewList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyReviewBinding binding = ItemMyReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MyReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReviewViewHolder holder, int position) {
        UserReview userReview = list.get(position);
        Review review = userReview.getReview();

        holder.binding.tvLocationName.setText(userReview.getLocationName());

        holder.binding.tvReviewTitle.setText(review.getTitle());
        holder.binding.tvReviewBody.setText(review.getBody());
        holder.binding.rbRating.setRating(review.getRating());
        holder.binding.tvReviewDate.setText(review.getCreatedAt());

        Glide.with(holder.itemView.getContext())
                .load(userReview.getLocationImage())
                .into(holder.binding.ivLocationThumb);

        // Sự kiện click sử dụng locationId từ UserReview
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLocationClick(userReview.getLocationId());
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class MyReviewViewHolder extends RecyclerView.ViewHolder {
        ItemMyReviewBinding binding;
        MyReviewViewHolder(ItemMyReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}