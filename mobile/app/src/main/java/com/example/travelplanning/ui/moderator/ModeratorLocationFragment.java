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

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.moderator.LocationReport;
import com.example.travelplanning.databinding.FragmentModeratorLocationBinding;
import com.example.travelplanning.databinding.ModeratorHeaderBinding;
import com.example.travelplanning.viewmodel.moderator.ModeratorLocationViewModel;

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
        popup.getMenu().add(0, 1, 1, "Delete Location");
        popup.getMenu().add(0, 2, 2, "Dismiss Report");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
             if (id == 1) {
                // TODO: Call API to delete location
                Toast.makeText(getContext(), "Location deleted", Toast.LENGTH_SHORT).show();
            } else if (id == 2) {
                viewModel.dismissReport(report.getReportId());
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
            binding.rvLocations.setVisibility((reports == null || reports.isEmpty()) ? View.GONE : View.VISIBLE);
        });
    }

    private void setupListeners() {
        headerBinding.btnReview.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_moderator_review));
        headerBinding.btnTrips.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_moderator_trip));
    }

}