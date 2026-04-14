package com.example.travelplanning.ui.itinerary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.databinding.ItemTripLocationBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripLocationAdapter extends RecyclerView.Adapter<TripLocationAdapter.ViewHolder> {

    // handle click
    public interface OnDeleteClickListener {
        void onDelete(ItineraryItem item);
    }
    public interface OnItemClickListener {
        void onItemClick(ItineraryItem item);
    }

    private OnDeleteClickListener deleteListener;
    private OnItemClickListener itemClickListener;

    public void setOnDeleteClickListener(OnDeleteClickListener l) {
        this.deleteListener = l;
    }
    public void setOnItemClickListener(OnItemClickListener l) {
        this.itemClickListener = l;
    }

    private List<ItineraryItem> items = new ArrayList<>();

    public void setItems(List<ItineraryItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTripLocationBinding binding = ItemTripLocationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTripLocationBinding binding;

        ViewHolder(ItemTripLocationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ItineraryItem item) {
            Location loc = item.getLocation();
            if (loc == null) return;

            // Name
            binding.tvLocationName.setText(loc.getName());

            // Rating
            if (loc.getAvgRating() != null) {
                String rating = String.format(Locale.US, "%.1f  (%d)",
                        loc.getAvgRating(),
                        loc.getRatingCount() != null ? loc.getRatingCount() : 0);
                binding.tvRating.setText(rating);
                binding.tvRating.setVisibility(View.VISIBLE);
            } else {
                binding.tvRating.setVisibility(View.GONE);
            }

            // Address
            if (loc.getAddress() != null && !loc.getAddress().isEmpty()) {
                binding.tvAddress.setText(loc.getAddress());
                binding.tvAddress.setVisibility(View.VISIBLE);
            } else {
                binding.tvAddress.setVisibility(View.GONE);
            }

            // Schedule status badge
            if (item.getDate() != null) {
                String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(item.getDate());
                String status = binding.getRoot().getContext().getString(R.string.schedule) + dateStr;
                binding.tvScheduleStatus.setText(status);
                binding.tvScheduleStatus.setBackgroundResource(R.drawable.bg_chip_scheduled);
            } else {
                binding.tvScheduleStatus.setText(R.string.unschedule);
                binding.tvScheduleStatus.setBackgroundResource(R.drawable.bg_chip_unscheduled);
            }

            // Image
            Glide.with(binding.ivLocationImage.getContext())
                    .load(loc.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(binding.ivLocationImage);

            // Delete
            binding.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(item);
            });
            binding.getRoot().setOnClickListener(v -> {
                if (itemClickListener != null) itemClickListener.onItemClick(item);
            });
        }
    }
}