package com.example.travelplanning.ui.location;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.location.Location;
import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<Location> locationList = new ArrayList<>();
    private OnLocationClickListener listener;

    public void setList(List<Location> newList) {
        this.locationList.clear(); 
        this.locationList.addAll(newList); 
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locationList.get(position);
        
        holder.tvName.setText(location.getName());
        
        double rating = location.getAvgRating() != null ? location.getAvgRating() : 0.0;
        int count = location.getRatingCount() != null ? location.getRatingCount() : 0;
        
        if (holder.tvRatingScore != null) {
            holder.tvRatingScore.setText(String.format(java.util.Locale.US, "%.1f (%d)", rating, count));
        }
        if (holder.rbAverageRating != null) {
            holder.rbAverageRating.setRating((float) rating);
        }

        holder.tvDistanceAddress.setText(location.getAddress());
        
        Glide.with(holder.itemView.getContext())
                .load(location.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.ivImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLocationClick(location);
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvDistanceAddress;
        TextView tvRatingScore; 
        android.widget.RatingBar rbAverageRating; 

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvDistanceAddress = itemView.findViewById(R.id.tvDistanceAddress);
            
            tvRatingScore = itemView.findViewById(R.id.tvRatingScore);
            rbAverageRating = itemView.findViewById(R.id.rbAverageRating);
        }
    }

    public interface OnLocationClickListener {
        void onLocationClick(Location location);
    }

    public void setOnLocationClickListener(OnLocationClickListener listener) {
        this.listener = listener;
    }
}