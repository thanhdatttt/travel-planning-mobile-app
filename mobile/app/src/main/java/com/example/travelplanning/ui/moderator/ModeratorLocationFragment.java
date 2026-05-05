package com.example.travelplanning.ui.moderator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.moderator.LocationReport;
import com.example.travelplanning.databinding.FragmentModeratorLocationBinding;
import com.example.travelplanning.databinding.ModeratorHeaderBinding;
import com.example.travelplanning.viewmodel.moderator.ModeratorLocationViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ModeratorLocationFragment extends Fragment {
    private FragmentModeratorLocationBinding binding;
    private ModeratorHeaderBinding headerBinding;
    private ModeratorLocationViewModel viewModel;
    private ModeratorLocationAdapter adapter;
    private final List<LocationReport> reportList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentModeratorLocationBinding.inflate(inflater, container, false);
        headerBinding = ModeratorHeaderBinding.bind(binding.moderatorHeader.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ModeratorLocationViewModel.class);

        headerBinding.btnLocations.setSelected(true);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.fetchReports(false);
    }

    private void setupRecyclerView() {
        adapter = new ModeratorLocationAdapter(reportList, this::showPopupMenu);
        binding.rvLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLocations.setAdapter(adapter);
    }

    private void showPopupMenu(View anchor, LocationReport report) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
//        popup.getMenu().add(0, 1, 1, getString(R.string.delete_location));
        popup.getMenu().add(0, 1, 1, R.string.dismiss_report);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                System.out.println(report);
                viewModel.dismissReport(report.getReportId());
                Toast.makeText(getContext(), R.string.report_dismissed, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                showLoading(getString(R.string.fetching_location_reports));
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

            // Only show empty state if it's NOT loading and the list is actually empty
            if (isEmpty && !isCurrentlyLoading) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvLocations.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvLocations.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if(error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.rvLocations.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        headerBinding.ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        headerBinding.btnReview.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_moderator_review));
        headerBinding.btnTrips.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_moderator_trip));
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