package com.example.travelplanning.ui.moderator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.databinding.ItemModeratorLocationBinding;
import java.util.List;

public class ModeratorLocationAdapter extends RecyclerView.Adapter<ModeratorLocationAdapter.LocationViewHolder> {
    private List<Report> reports;
    private final OnReportActionListener listener;

    public interface OnReportActionListener {
        void onOptionClick(View anchor, Report report);
    }

    public ModeratorLocationAdapter(List<Report> reports, OnReportActionListener listener) {
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemModeratorLocationBinding binding = ItemModeratorLocationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new LocationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        holder.bind(reports.get(position), listener);
    }

    @Override
    public int getItemCount() { return reports != null ? reports.size() : 0; }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        private final ItemModeratorLocationBinding binding;

        public LocationViewHolder(ItemModeratorLocationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Report report, OnReportActionListener listener) {
            binding.tvDescription.setMaxLines(4);
            binding.tvReadMore.setVisibility(View.GONE);

            // TODO: Map from Location Report DTO
//            binding.tvLocationName.setText(report.getName());
            binding.tvReportReason.setText(report.getReason());
            binding.tvReportedBy.setText(report.getReporterId());

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