package com.example.travelplanning.ui.favorite;

import android.view.LayoutInflater;
import android.view.View; // Import View
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.ItemSmallTripBinding;

import java.util.Objects;

public class FavoriteAdapter extends ListAdapter<Itinerary, FavoriteAdapter.FavoriteViewHolder> {

    private final OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onItemClick(Itinerary itinerary);
    }

    public FavoriteAdapter(OnFavoriteClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Itinerary> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Itinerary>() {
                @Override
                public boolean areItemsTheSame(@NonNull Itinerary oldItem, @NonNull Itinerary newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Itinerary oldItem, @NonNull Itinerary newItem) {
                    return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                            Objects.equals(oldItem.getImage(), newItem.getImage());
                }
            };

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSmallTripBinding binding = ItemSmallTripBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoriteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final ItemSmallTripBinding binding;

        FavoriteViewHolder(@NonNull ItemSmallTripBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Itinerary itinerary) {
            binding.tvSmallTripName.setText(itinerary.getTitle());

            // Ẩn ngày tháng
            binding.tvSmallTripDates.setVisibility(View.GONE);
            binding.btnConfig.setVisibility(View.GONE);

            String imageUrl = itinerary.getImage();

            Glide.with(itemView.getContext())
                    .load(imageUrl != null && !imageUrl.isEmpty() ? imageUrl : R.drawable.ic_placeholder)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .centerCrop()
                    .into(binding.ivSmallCover);

            binding.getRoot().setOnClickListener(v -> listener.onItemClick(itinerary));
        }
    }
}