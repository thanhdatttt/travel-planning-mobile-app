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
import com.example.travelplanning.data.model.report.Report;
import com.example.travelplanning.databinding.FragmentModeratorTripBinding;
import com.example.travelplanning.databinding.ModeratorHeaderBinding;
import com.example.travelplanning.viewmodel.moderator.ModeratorReviewViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ModeratorTripFragment extends Fragment {
    private FragmentModeratorTripBinding binding;
    private ModeratorHeaderBinding headerBinding;
    private ModeratorReviewViewModel viewModel;
    private ModeratorTripAdapter adapter;
    private final List<Report> reportList = new ArrayList<>();

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
        viewModel = new ViewModelProvider(this).get(ModeratorReviewViewModel.class);

        viewModel.setFilterType("itinerary");

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

    private void showPopupMenu(View anchor, Report report) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Ban Creator");
        popup.getMenu().add(0, 2, 1, "Delete Trip");
        popup.getMenu().add(0, 3, 2, "Dismiss Report");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                showBanConfirmationDialog(report);
            } else if (id == 2) {
                // TODO: Delegate to Service/API layer to delete the itinerary
                Toast.makeText(getContext(), "Trip deleted", Toast.LENGTH_SHORT).show();
            } else if (id == 3) {
                viewModel.dismissReport(report.getId());
                Toast.makeText(getContext(), "Report dismissed", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }

    private void setupObservers() {
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
        headerBinding.btnReview.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_moderator_review));
        headerBinding.btnLocations.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_moderator_location));
    }

    private void showBanConfirmationDialog(Report report) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.are_you_sure_you_want_to_ban_the_user_who_created_this + report.getTargetType() + "?")
                .setPositiveButton(R.string.ban, (dialog, which) -> viewModel.banUserFromReport(report.getTargetId()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}