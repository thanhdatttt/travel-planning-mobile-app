package com.example.travelplanning.ui.itinerary;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
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
                            && Objects.equals(oldItem.getEndDate(), newItem.getEndDate());
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

    class TripViewHolder extends RecyclerView.ViewHolder {
        private final @UnknownNullability ItemSmallTripBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        TripViewHolder(@UnknownNullability ItemSmallTripBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Itinerary itinerary) {
            binding.tvSmallTripName.setText(itinerary.getTitle());

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