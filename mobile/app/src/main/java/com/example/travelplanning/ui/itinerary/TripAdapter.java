package com.example.travelplanning.ui.itinerary;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.ItemSmallTripBinding;
import org.jetbrains.annotations.UnknownNullability;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class TripAdapter extends ListAdapter<Itinerary, TripAdapter.TripViewHolder> {

    private final OnTripClickListener onTripClick;
    private final OnConfigClickListener onConfigClick;

    // handle click from fragment
    // trip click
    public interface OnTripClickListener {
        void onClick(Itinerary itinerary);
    }
    // config button click
    public interface OnConfigClickListener {
        void onConfigClick(Itinerary itinerary);
    }

    public TripAdapter(OnTripClickListener onTripClick, OnConfigClickListener onConfigClick) {
        super(DIFF_CALLBACK);
        this.onTripClick = onTripClick;
        this.onConfigClick = onConfigClick;
    }

    // DiffUtil to compare and update only changed items in recycle view
    private static final DiffUtil.ItemCallback<Itinerary> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Itinerary>() {
                @Override
                public boolean areItemsTheSame(@NonNull Itinerary oldItem, @NonNull Itinerary newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Itinerary oldItem, @NonNull Itinerary newItem) {
                    return Objects.equals(oldItem.getTitle(), newItem.getTitle())
                            && Objects.equals(oldItem.getStartDate(), newItem.getStartDate())
                            && Objects.equals(oldItem.getEndDate(), newItem.getEndDate())
                            && Objects.equals(oldItem.getPrivacy(), newItem.getPrivacy())
                            && Objects.equals(getPreviewImageUrl(oldItem), getPreviewImageUrl(newItem));
                }
            };

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSmallTripBinding binding = ItemSmallTripBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TripViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    // Safe helper — never throws regardless of null chain depth
    private static String getPreviewImageUrl(Itinerary itinerary) {
        String imageUrl = null;
        if (itinerary.getItineraryItems() != null && !itinerary.getItineraryItems().isEmpty()) {
            var firstItem = itinerary.getItineraryItems().get(0);
            if (firstItem != null && firstItem.getLocation() != null) {
                imageUrl = firstItem.getLocation().getImageUrl();
            }
        }
        return imageUrl;
    }

    class TripViewHolder extends RecyclerView.ViewHolder {
        private final @UnknownNullability ItemSmallTripBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        TripViewHolder(@UnknownNullability ItemSmallTripBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Itinerary itinerary) {
            binding.tvSmallTripName.setText(itinerary.getTitle());

            // image cover
            String imageUrl = null;
            if (itinerary.getItineraryItems() != null && !itinerary.getItineraryItems().isEmpty()) {
                var firstItem = itinerary.getItineraryItems().get(0);
                if (firstItem != null && firstItem.getLocation() != null) {
                    imageUrl = firstItem.getLocation().getImageUrl();
                }
            }

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder) // image when loading
                        .error(R.drawable.ic_placeholder)       // image when error
                        .centerCrop()
                        .into(binding.ivSmallCover);
            } else {
                // clear resource and set default image if source image is empty
                Glide.with(itemView.getContext()).clear(binding.ivSmallCover);
                binding.ivSmallCover.setImageResource(R.drawable.ic_placeholder);
            }

            // format date
            if (itinerary.getStartDate() != null && itinerary.getEndDate() != null) {
                String dates = dateFormat.format(itinerary.getStartDate()) + " – " + dateFormat.format(itinerary.getEndDate());
                binding.tvSmallTripDates.setText(dates);
            } else {
                binding.tvSmallTripDates.setText("");
            }

            // handle click
            binding.getRoot().setOnClickListener(v -> onTripClick.onClick(itinerary));
            binding.btnConfig.setOnClickListener(v -> onConfigClick.onConfigClick(itinerary));
        }
    }
}