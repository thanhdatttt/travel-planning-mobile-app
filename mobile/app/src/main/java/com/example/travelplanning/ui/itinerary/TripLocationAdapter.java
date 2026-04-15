package com.example.travelplanning.ui.itinerary;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.databinding.ItemTripLocationBinding;

import org.jetbrains.annotations.UnknownNullability;
import java.util.Locale;
import java.util.Objects;

public class TripLocationAdapter extends ListAdapter<ItineraryItem, TripLocationAdapter.ViewHolder> {

    // handle click
    public interface OnDeleteClickListener {
        void onDelete(ItineraryItem item);
    }

    public interface OnItemClickListener {
        void onItemClick(ItineraryItem item);
    }

    private final OnDeleteClickListener deleteListener;
    private final OnItemClickListener itemClickListener;

    public TripLocationAdapter(OnItemClickListener itemClickListener, OnDeleteClickListener deleteListener) {
        super(DIFF_ITEM_CALLBACK);
        this.itemClickListener = itemClickListener;
        this.deleteListener = deleteListener;
    }

    // DiffUtil to compare and update only changed items in recycle view
    private static final DiffUtil.ItemCallback<ItineraryItem> DIFF_ITEM_CALLBACK =
            new DiffUtil.ItemCallback<ItineraryItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull ItineraryItem oldItem, @NonNull ItineraryItem newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ItineraryItem oldItem, @NonNull ItineraryItem newItem) {
                    return Objects.equals(oldItem.getDate(), newItem.getDate())
                            && Objects.equals(oldItem.getNote(), newItem.getNote())
                            && Objects.equals(oldItem.getOrderIdx(), newItem.getOrderIdx())
                            && Objects.equals(oldItem.getLocationId(), newItem.getLocationId()); // ← ADD
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTripLocationBinding binding = ItemTripLocationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final @UnknownNullability ItemTripLocationBinding binding;

        ViewHolder(@UnknownNullability ItemTripLocationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ItineraryItem item) {
            Location loc = item.getLocation();
            Log.d("DEBUG", "location: " + loc);
            if (loc == null) {
                return;
            }

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

            // Image
            Glide.with(binding.ivLocationImage.getContext())
                    .load(loc.getImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(binding.ivLocationImage);

            // handle click
            binding.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(item);
            });
            binding.getRoot().setOnClickListener(v -> {
                if (itemClickListener != null) itemClickListener.onItemClick(item);
            });
        }
    }
}