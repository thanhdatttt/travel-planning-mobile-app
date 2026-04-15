package com.example.travelplanning.ui.itinerary;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.ItemPublicTripBinding;

import org.jetbrains.annotations.UnknownNullability;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

public class TripPublicAdapter extends ListAdapter<Itinerary, TripPublicAdapter.TripPublicViewHolder> {
    private final OnTripClickListener onTripClick;

    // handle trip click
    public interface OnTripClickListener {
        void onClick(Itinerary itinerary);
    }

    public TripPublicAdapter(OnTripClickListener onTripClick) {
        super(DIFF_CALLBACK);
        this.onTripClick = onTripClick;
    }

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
                            && Objects.equals(oldItem.getEndDate(), newItem.getEndDate());
                }
            };

    @NonNull
    @Override
    public TripPublicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPublicTripBinding binding = ItemPublicTripBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TripPublicViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TripPublicViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TripPublicViewHolder extends RecyclerView.ViewHolder {
        private final @UnknownNullability ItemPublicTripBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        TripPublicViewHolder(@UnknownNullability ItemPublicTripBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Itinerary itinerary) {
            binding.tvTripName.setText(itinerary.getTitle());

            // creator info
            binding.tvCreatorName.setText(itinerary.getUser().getUsername());

            // load image cover
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
                        .into(binding.ivCover);
            } else {
                // clear resource and set default image if source image is empty
                Glide.with(itemView.getContext()).clear(binding.ivCover);
                binding.ivCover.setImageResource(R.drawable.ic_placeholder);
            }


            // format date
            if (itinerary.getStartDate() != null && itinerary.getEndDate() != null) {
                String dates = dateFormat.format(itinerary.getStartDate()) + " – " + dateFormat.format(itinerary.getEndDate());
                binding.tvTripDates.setText(dates);
            } else {
                binding.tvTripDates.setText("");
            }

            // handle click
            binding.getRoot().setOnClickListener(v -> onTripClick.onClick(itinerary));
        }
    }
}
