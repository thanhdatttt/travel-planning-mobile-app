package com.example.travelplanning.ui.moderator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.moderator.ReviewReport;
import com.example.travelplanning.databinding.ItemModeratorReviewBinding;

import java.util.List;

public class ModeratorReviewAdapter extends RecyclerView.Adapter<ModeratorReviewAdapter.ReportViewHolder> {
    private List<ReviewReport> reports;
    private final OnReportActionListener listener;

    public interface OnReportActionListener {
        void onOptionClick(View anchor, ReviewReport report);
    }

    public ModeratorReviewAdapter(List<ReviewReport> reports, OnReportActionListener listener) {
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

        public void bind(ReviewReport report, OnReportActionListener listener) {
            binding.tvReviewText.setMaxLines(4);
            binding.tvReadMore.setVisibility(View.GONE);

            binding.tvReviewText.setText(report.getReviewText());
            binding.tvReviewerUsername.setText(report.getReviewerName());

            int reasonResId = getLocalizedReasonResId(report.getReportReason());
            String localizedReason;
            if (reasonResId != -1) localizedReason = binding.getRoot().getContext().getString(reasonResId);
            else localizedReason = report.getReportReason();
            binding.tvReportReason.setText(localizedReason);

            binding.tvReportedBy.setText(report.getReporterName());

            binding.tvTitle.setText(report.getTitle());
            binding.tvRating.setText(report.getFormattedRating());

            binding.tvReviewText.post(() -> {
                if (binding.tvReviewText.getLineCount() > 4) {
                    binding.tvReadMore.setVisibility(View.VISIBLE);
                } else {
                    binding.tvReadMore.setVisibility(View.GONE);
                }
            });

            if (report.getAvatarUrl() != null && !report.getAvatarUrl().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(report.getAvatarUrl())
                        .placeholder(R.drawable.ic_user) // Assuming you have a user placeholder
                        .error(R.drawable.ic_user)
                        .circleCrop() // Circular crop looks best for avatars
                        .into(binding.imgAvatar); // Change ID if necessary
            } else {
                Glide.with(binding.getRoot().getContext()).clear(binding.imgAvatar);
                binding.imgAvatar.setImageResource(R.drawable.ic_user);
            }

            binding.tvDate.setText(report.getReviewDate());

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