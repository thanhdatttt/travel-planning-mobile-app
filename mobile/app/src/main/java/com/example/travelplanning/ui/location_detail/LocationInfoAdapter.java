package com.example.travelplanning.ui.location;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationInfoAdapter extends RecyclerView.Adapter<LocationInfoAdapter.LocationViewHolder> {
    private List<LocationInfoItem> items;

    public LocationInfoAdapter(List<LocationInfoItem> items) { this.items = items; }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationInfoItem item = items.get(position);
        holder.ivIcon.setImageResource(item.getIconRes());
        holder.tvTitle.setText(item.getTitle());
        holder.tvContent.setText(item.getContent());
        holder.tvContent.setTextColor(item.getContentColor());
    }
    // ... onCreateViewHolder và getItemCount
}