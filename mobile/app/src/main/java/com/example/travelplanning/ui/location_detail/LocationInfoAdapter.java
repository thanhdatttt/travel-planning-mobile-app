package com.example.travelplanning.ui.location_detail;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;
import com.example.travelplanning.databinding.ItemLocationInfoBinding;

import java.util.List;

public class LocationInfoAdapter extends RecyclerView.Adapter<LocationInfoAdapter.ViewHolder> {

    private final List<LocationInfoItem> items;

    public LocationInfoAdapter(List<LocationInfoItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLocationInfoBinding binding = ItemLocationInfoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationInfoItem item = items.get(position);

        holder.binding.ivIcon.setImageResource(item.getIconRes());
        holder.binding.tvTitle.setText(item.getTitle());
        holder.binding.tvContent.setText(item.getContent());
        if (item.getContentColor() != 0)
            holder.binding.tvContent.setTextColor(item.getContentColor());

    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // Giữ lại đối tượng binding
        final ItemLocationInfoBinding binding;

        ViewHolder(ItemLocationInfoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}