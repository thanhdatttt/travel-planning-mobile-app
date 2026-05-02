package com.example.travelplanning.ui.moderator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.moderator.ItineraryReport;
import com.example.travelplanning.databinding.ItemModeratorTripBinding;
import java.util.List;

public class ModeratorTripAdapter extends RecyclerView.Adapter<ModeratorTripAdapter.TripViewHolder> {
    private List<ItineraryReport> reports;
    private final OnReportActionListener listener;

    public interface OnReportActionListener {
        void onOptionClick(View anchor, ItineraryReport report);
    }

    public ModeratorTripAdapter(List<ItineraryReport> reports, OnReportActionListener listener) {
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemModeratorTripBinding binding = ItemModeratorTripBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TripViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        holder.bind(reports.get(position), listener);
    }

    @Override
    public int getItemCount() { return reports != null ? reports.size() : 0; }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        private final ItemModeratorTripBinding binding;

        public TripViewHolder(ItemModeratorTripBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private int getLocalizedReasonResId(String rawReason) {
            if (rawReason == null) return R.string.reason;

            switch (rawReason.toLowerCase()) {
                case "spam":
                    return R.string.spam;
                case "inappropriate":
                    return R.string.inappropriate;
                case "irrelevant":
                    return R.string.irrelevant;
                default:
                    return -1;
            }
        }

        public void bind(ItineraryReport report, OnReportActionListener listener) {
            binding.tvDescription.setMaxLines(4);
            binding.tvReadMore.setVisibility(View.GONE);

            binding.tvTripTitle.setText(report.getTitle());
            binding.tvStartDate.setText(report.getFormattedStartDate());
            binding.tvEndDate.setText(report.getFormattedEndDate());
            binding.tvDescription.setText(report.getDescription());

            int reasonResId = getLocalizedReasonResId(report.getReportReason());
            String localizedReason;
            if(reasonResId != -1) localizedReason = binding.getRoot().getContext().getString(reasonResId);
            else localizedReason = report.getReportReason();
            binding.tvReportReason.setText(localizedReason);

            binding.tvReportedBy.setText(report.getReporterName());

            binding.tvReportedBy.setText(report.getReporterName());

            binding.tvDescription.post(() -> {
                if (binding.tvDescription.getLineCount() > 4) {
                    binding.tvReadMore.setVisibility(View.VISIBLE);
                } else {
                    binding.tvReadMore.setVisibility(View.GONE);
                }
            });

            if (report.getAvatarUrl() != null && !report.getAvatarUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(report.getAvatarUrl())
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .circleCrop()
                        .into(binding.imgTripAvatar); // Change ID if necessary
            } else {
                Glide.with(binding.getRoot().getContext()).clear(binding.imgTripAvatar);
                binding.imgTripAvatar.setImageResource(R.drawable.ic_user);
            }

            binding.tvReadMore.setOnClickListener(v -> {
                binding.tvDescription.setMaxLines(Integer.MAX_VALUE);
                binding.tvReadMore.setVisibility(View.GONE);
            });

            binding.btnMoreOptions.setOnClickListener(v -> listener.onOptionClick(v, report));
        }
    }
}