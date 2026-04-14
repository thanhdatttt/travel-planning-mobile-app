package com.example.travelplanning.ui.itinerary;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.databinding.FragmentTripLocationListBinding;
import com.example.travelplanning.ui.location.LocationSearchActivity;
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class TripLocationListFragment extends Fragment {
    private FragmentTripLocationListBinding binding;
    private ItineraryViewModel viewModel;
    private TripLocationAdapter adapter;
    private String currentTripId;

    private final ActivityResultLauncher<Intent> pickLocationLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == AppCompatActivity.RESULT_OK
                                && result.getData() != null) {

                            String locationId = result.getData()
                                    .getStringExtra(LocationSearchActivity.EXTRA_SELECTED_LOCATION_ID);

                            if (locationId != null && currentTripId != null) {
                                viewModel.addItineraryItem(currentTripId, locationId, null);
                            }
                        }
                    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTripLocationListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        adapter = new TripLocationAdapter();
        binding.rvLocations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLocations.setAdapter(adapter);

        // item click
        adapter.setOnItemClickListener(item -> {

        });
        // delete
        adapter.setOnDeleteClickListener(item ->
                viewModel.deleteItineraryItem(currentTripId, item.getId())
        );
    }

    private void setupObservers() {
        viewModel.getSelectedItinerary().observe(getViewLifecycleOwner(), itinerary -> {
            if (itinerary == null) return;

            currentTripId = itinerary.getId(); // keep cached for the launcher callback

            List<ItineraryItem> items = itinerary.getItineraryItems();
            if (items == null || items.isEmpty()) {
                adapter.setItems(null);
                binding.tvLocationCount.setText("0 locations");
            } else {
                adapter.setItems(items);
                String locationNum = items.size() + (items.size() == 1 ? " location" : " locations");
                binding.tvLocationCount.setText(locationNum);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                SnackBarHelper.showTopSnackBar(requireView(), msg, SnackBarHelper.SnackBarType.ERROR);
            }
        });
    }

    private void setupListeners() {
        binding.btnAddLocation.setOnClickListener(v -> {
            if (currentTripId == null) return;

            Intent intent = new Intent(requireContext(), LocationSearchActivity.class);
            intent.putExtra(LocationSearchActivity.EXTRA_MODE, LocationSearchActivity.MODE_PICK);
            intent.putExtra(LocationSearchActivity.EXTRA_ITINERARY_ID, currentTripId);
            pickLocationLauncher.launch(intent);
        });
    }
}
