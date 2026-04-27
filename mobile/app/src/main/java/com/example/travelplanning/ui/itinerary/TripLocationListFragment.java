package com.example.travelplanning.ui.itinerary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.databinding.FragmentTripLocationListBinding;
import com.example.travelplanning.ui.location.LocationSearchActivity;
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TripLocationListFragment extends Fragment {
    private FragmentTripLocationListBinding binding;
    private ItineraryViewModel viewModel;
    private final TripLocationAdapter adapter = new TripLocationAdapter(this::navigateToLocationDetail, this::deleteLocation);
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
        binding.rvLocations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLocations.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getSelectedItinerary().observe(getViewLifecycleOwner(), itinerary -> {
            if (itinerary == null) return;

            currentTripId = itinerary.getId(); // keep cached for the launcher callback

            showLocationList(itinerary);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
//            binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
//            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                SnackBarHelper.showTopSnackBar(requireView(), msg, SnackBarHelper.SnackBarType.ERROR);
            }
        });

        viewModel.getAddItemSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                viewModel.getAddItemSuccess().setValue(false);
            }
        });

        viewModel.getDeleteItemSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                viewModel.getDeleteItemSuccess().setValue(false);
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

    // show location list
    private void showLocationList(Itinerary itinerary) {
        List<ItineraryItem> items = itinerary.getItineraryItems();

        if (items == null || items.isEmpty()) {
            binding.tvLocationCount.setText("0 locations");
            adapter.submitList(Collections.emptyList());
        } else {
            String locationNum = items.size() + (items.size() == 1 ? " location" : " locations");
            binding.tvLocationCount.setText(locationNum);
            adapter.submitList(new ArrayList<>(items));
        }
    }

    private void confirmAndDelete(ItineraryItem item) {
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_delete)
                .setTitle("Remove this location?")
                .setMessage("Are you sure you want to remove this location? This cannot be undone.")
                .setPositiveButton("Remove", (d, which) -> {
                    if (currentTripId != null)
                        viewModel.deleteItineraryItem(currentTripId, item.getId());
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(getResources().getColor(R.color.error, requireContext().getTheme()));
    }

    // handle click of location
    private void navigateToLocationDetail(ItineraryItem item) {

    }

    // handle click of delete location
    private void deleteLocation(ItineraryItem item) {
        confirmAndDelete(item);
    }
}
