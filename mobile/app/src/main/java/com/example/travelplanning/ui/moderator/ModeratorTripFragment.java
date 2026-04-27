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
import com.example.travelplanning.data.model.moderator.ItineraryReport;
import com.example.travelplanning.databinding.FragmentModeratorTripBinding;
import com.example.travelplanning.databinding.ModeratorHeaderBinding;
import com.example.travelplanning.viewmodel.moderator.ModeratorItineraryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ModeratorTripFragment extends Fragment {
    private FragmentModeratorTripBinding binding;
    private ModeratorHeaderBinding headerBinding;
    private ModeratorItineraryViewModel viewModel;
    private ModeratorTripAdapter adapter;
    private final List<ItineraryReport> reportList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentModeratorTripBinding.inflate(inflater, container, false);
        headerBinding = ModeratorHeaderBinding.bind(binding.moderatorHeader.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ModeratorItineraryViewModel.class);

        if(headerBinding.btnTrips != null) {
            headerBinding.btnTrips.setSelected(true);
        }

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.fetchReports(false);
    }

    private void setupRecyclerView() {
        adapter = new ModeratorTripAdapter(reportList, this::showPopupMenu);
        binding.rvTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTrips.setAdapter(adapter);
    }

    private void showPopupMenu(View anchor, ItineraryReport report) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, R.string.ban_creator);
        popup.getMenu().add(0, 2, 1, R.string.dismiss_report);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                showBanConfirmationDialog(report);
            } else if (id == 2) {
                viewModel.dismissReport(report.getReportId());
                Toast.makeText(getContext(), "Report dismissed", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                showLoading(getString(R.string.fetching_itinerary_reports));
            } else {
                hideLoading();
            }
        });

        viewModel.getReports().observe(getViewLifecycleOwner(), reports -> {
            if (reports != null) {
                reportList.clear();
                reportList.addAll(reports);
                adapter.notifyDataSetChanged();
            }
            binding.tvEmptyState.setVisibility((reports == null || reports.isEmpty()) ? View.VISIBLE : View.GONE);
            binding.rvTrips.setVisibility((reports == null || reports.isEmpty()) ? View.GONE : View.VISIBLE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if(error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        headerBinding.ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
        headerBinding.btnReview.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_moderator_review));
        headerBinding.btnLocations.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_moderator_location));
    }

    private void showBanConfirmationDialog(ItineraryReport report) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.are_you_sure_you_want_to_ban_the_user + " " + report.getOwnerName() + "?")
                .setPositiveButton(R.string.ban, (dialog, which) -> viewModel.banUserFromReport(report.getOwnerId()))
                .setNegativeButton(R.string.cancel, null)
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