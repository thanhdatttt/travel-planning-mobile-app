package com.example.travelplanning.ui.moderator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.moderator.ReviewReport;
import com.example.travelplanning.databinding.FragmentModeratorReviewBinding;
import com.example.travelplanning.databinding.ModeratorHeaderBinding;
import com.example.travelplanning.viewmodel.moderator.ModeratorReviewViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ModeratorReviewFragment extends Fragment {
    private FragmentModeratorReviewBinding binding;
    private ModeratorHeaderBinding headerBinding;
    private ModeratorReviewViewModel viewModel;
    private ModeratorReviewAdapter adapter;
    private final List<ReviewReport> reportList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentModeratorReviewBinding.inflate(inflater, container, false);
        headerBinding = ModeratorHeaderBinding.bind(binding.moderatorHeader.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ModeratorReviewViewModel.class);
        headerBinding.btnReview.setSelected(true);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.fetchReports(false);
    }

    private void setupRecyclerView() {
        adapter = new ModeratorReviewAdapter(reportList, this::showPopupMenu);

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUsers.setAdapter(adapter);
    }

    private void showPopupMenu(View anchor, ReviewReport report) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, R.string.ban_user);
        popup.getMenu().add(0, 2, 1, R.string.dismiss_report);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                showBanConfirmationDialog(report);
                return true;
            } else if (id == 2) {
                viewModel.dismissReport(report.getReportId());
                Toast.makeText(getContext(), "Report dismissed", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                showLoading(getString(R.string.fetching_review_reports));
            } else {
                hideLoading();
            }
        });

        viewModel.getReports().observe(getViewLifecycleOwner(), reports -> {
            boolean isEmpty = reports == null || reports.isEmpty();
            boolean isCurrentlyLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());

            if (reports != null) {
                reportList.clear();
                reportList.addAll(reports);
                adapter.notifyDataSetChanged();
            }

            if (isEmpty && !isCurrentlyLoading) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvUsers.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvUsers.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        binding.rvUsers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0) {
                        viewModel.fetchReports(true);
                    }
                }
            }
        });

        headerBinding.btnLocations.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_moderator_location);
        });

        headerBinding.btnTrips.setOnClickListener( v -> {
            Navigation.findNavController(v).navigate(R.id.nav_moderator_trip);
        });
    }

    private void showBanConfirmationDialog(ReviewReport report) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.are_you_sure_you_want_to_ban_the_user + " " + report.getReviewerName() + "?")
                .setPositiveButton(R.string.ban, (dialog, which) -> {
                    viewModel.banUserFromReport(report.getReviewerId());
                })
                .setNegativeButton( R.string.cancel , null)
                .show();
    }

    private void showLoading(String message) {
        if (binding == null) return;
        binding.loadingOverlay.getRoot().setVisibility(View.VISIBLE);
        binding.loadingOverlay.tvLoadingMessage.setText(message);
    }

    private void hideLoading() {
        if (binding == null) return;
        binding.loadingOverlay.getRoot().setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}