package com.example.travelplanning.ui.moderator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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

        public void bind(ItineraryReport report, OnReportActionListener listener) {
            binding.tvDescription.setMaxLines(4);
            binding.tvReadMore.setVisibility(View.GONE);

            binding.tvTripTitle.setText(report.getTitle());
            binding.tvStartDate.setText(report.getFormattedStartDate());
            binding.tvEndDate.setText(report.getFormattedEndDate());
            binding.tvDescription.setText(report.getDescription());
            binding.tvReportReason.setText(report.getReportReason());
            binding.tvReportedBy.setText(report.getReporterName());

            binding.tvReportReason.setText(report.getReportReason());
            binding.tvReportedBy.setText(report.getReporterName());

            binding.tvDescription.post(() -> {
                if (binding.tvDescription.getLineCount() > 4) {
                    binding.tvReadMore.setVisibility(View.VISIBLE);
                } else {
                    binding.tvReadMore.setVisibility(View.GONE);
                }
            });

            binding.tvReadMore.setOnClickListener(v -> {
                binding.tvDescription.setMaxLines(Integer.MAX_VALUE);
                binding.tvReadMore.setVisibility(View.GONE);
            });

            binding.btnMoreOptions.setOnClickListener(v -> listener.onOptionClick(v, report));
        }
    }
}