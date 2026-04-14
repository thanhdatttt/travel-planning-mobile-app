package com.example.travelplanning.ui.moderator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.databinding.ItemModeratorReviewBinding; // Ensure this matches your layout file name

import java.util.List;

public class ModeratorReviewAdapter extends RecyclerView.Adapter<ModeratorReviewAdapter.ReportViewHolder> {
    private List<Report> reports;
    private final OnReportActionListener listener;

    // Interface đã được tối giản để chỉ truyền View anchor và Data
    public interface OnReportActionListener {
        void onOptionClick(View anchor, Report report);
    }

    public ModeratorReviewAdapter(List<Report> reports, OnReportActionListener listener) {
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemModeratorReviewBinding binding = ItemModeratorReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReportViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.bind(reports.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        private final ItemModeratorReviewBinding binding;

        public ReportViewHolder(ItemModeratorReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Report report, OnReportActionListener listener) {
            binding.tvReviewText.setMaxLines(4);
            binding.tvReadMore.setVisibility(View.GONE);

//            binding.tvReviewText.setText(report.getReviewContent());
//            binding.tvReviewerUsername.setText(report.getReviewerName());
//            binding.tvPlaceName.setText(report.getPlaceName());
//            binding.tvReportReason.setText(report.getReason());
//            binding.tvReportedBy.setText(report.getReporterName());

            binding.tvReviewText.post(() -> {
                if (binding.tvReviewText.getLineCount() > 4) {
                    binding.tvReadMore.setVisibility(View.VISIBLE);
                } else {
                    binding.tvReadMore.setVisibility(View.GONE);
                }
            });

            binding.tvReadMore.setOnClickListener(v -> {
                binding.tvReviewText.setMaxLines(Integer.MAX_VALUE);
                binding.tvReadMore.setVisibility(View.GONE);
            });

            binding.btnMoreOptions.setOnClickListener(v -> {
                listener.onOptionClick(v, report);
            });
        }
    }

}