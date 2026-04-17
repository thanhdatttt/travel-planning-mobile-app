package com.example.travelplanning.ui.itinerary;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.HeaderItem;
import com.example.travelplanning.data.model.itinerary.ItineraryDisplayItem;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.model.itinerary.LocationItem;
import com.example.travelplanning.data.model.location.Location;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItineraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ItineraryDisplayItem> items = new ArrayList<>();
    private OnItineraryItemClickListener listener;

    // interface handle click event
    public interface OnItineraryItemClickListener {
        void onItemClicked(ItineraryItem item);
        void onDeleteClick(ItineraryItem item);
        void onUnscheduleClick(ItineraryItem item);
        void onAddLocationClick(Date date);
    }

    public void setData(List<ItineraryDisplayItem> items, OnItineraryItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ItineraryDisplayItem.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_itinerary_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_itinerary_location, parent, false);
            return new LocationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItineraryDisplayItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((HeaderItem) item, listener);
        } else if (holder instanceof LocationViewHolder) {
            ((LocationViewHolder) holder).bind((LocationItem) item, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // --- ViewHolder for date header
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderDayTitle;
        Button btnAddLocation;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderDayTitle = itemView.findViewById(R.id.tvHeaderDayTitle);
            btnAddLocation = itemView.findViewById(R.id.btnAddLocation);
        }

        void bind(HeaderItem header, OnItineraryItemClickListener listener) {
            tvHeaderDayTitle.setText(sdf.format(header.getDate()));
            btnAddLocation.setOnClickListener(v -> {
                if (listener != null)
                    listener.onAddLocationClick(header.getDate());
            });
        }
    }

    // --- ViewHolder for location item
    static class LocationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLocationImage;
        TextView tvLocationName, tvRating, tvAddress;
        Button btnUnschedule;
        ImageButton btnDelete;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLocationImage = itemView.findViewById(R.id.ivLocationImage);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            btnUnschedule = itemView.findViewById(R.id.btnUnschedule);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(LocationItem locationItem, OnItineraryItemClickListener listener) {
            ItineraryItem data = locationItem.getItem();
            Location loc = data.getLocation();
            Log.d("DEBUG", "location: " + loc);
            if (loc == null) {
                return;
            }

            // name
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
            // address
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

            // click event
            btnDelete.setOnClickListener(v -> {
                if (listener != null)
                    listener.onDeleteClick(data);
            });

            btnUnschedule.setOnClickListener(v -> {
                if (listener != null)
                    listener.onUnscheduleClick(data);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onItemClicked(data);
            });
        }
    }
}