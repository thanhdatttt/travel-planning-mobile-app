package com.example.travelplanning.ui.itinerary;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.model.location.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationSelectionAdapter extends RecyclerView.Adapter<LocationSelectionAdapter.ViewHolder> {
    private List<ItineraryItem> items = new ArrayList<>();
    private final OnLocationSelectedListener listener;

    public interface OnLocationSelectedListener {
        void onLocationSelected(ItineraryItem item);
    }

    public LocationSelectionAdapter(OnLocationSelectedListener listener) {
        this.listener = listener;
    }

    public void setData(List<ItineraryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng lại layout item location bạn đã có hoặc tạo layout đơn giản hơn
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_public_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLocationImage;
        TextView tvLocationName, tvAddress, tvRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLocationImage = itemView.findViewById(R.id.ivLocationImage);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRating = itemView.findViewById(R.id.tvRating);
        }

        void bind(ItineraryItem item, OnLocationSelectedListener listener) {
            Location loc = item.getLocation();
            Log.d("DEBUG", "location: " + loc);
            if (loc == null) {
                return;
            }

            tvLocationName.setText(loc.getName());
            // rating
            if (loc.getAvgRating() != null) {
                String rating = String.format(Locale.US, "%.1f  (%d)",
                        loc.getAvgRating(),
                        loc.getRatingCount() != null ? loc.getRatingCount() : 0);
                tvRating.setText(rating);
                tvRating.setVisibility(View.VISIBLE);
            } else {
                tvRating.setVisibility(View.GONE);
            }
            // Address
            if (loc.getAddress() != null && !loc.getAddress().isEmpty()) {
                tvAddress.setText(loc.getAddress());
                tvAddress.setVisibility(View.VISIBLE);
            } else {
                tvAddress.setVisibility(View.GONE);
            }
            // Image
            Glide.with(ivLocationImage.getContext())
                    .load(loc.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(ivLocationImage);

            itemView.setOnClickListener(v -> listener.onLocationSelected(item));
        }
    }
}