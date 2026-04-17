package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.data.model.itinerary.ItineraryDisplayItem;
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.databinding.FragmentItineraryBinding;
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ItineraryFragment extends Fragment {
    private FragmentItineraryBinding binding;
    private ItineraryViewModel viewModel;
    private final ItineraryAdapter adapter = new ItineraryAdapter();
    private String currentTripId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentItineraryBinding.inflate(inflater, container, false);
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
        binding.rvItinerary.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvItinerary.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getSelectedItinerary().observe(getViewLifecycleOwner(), itinerary -> {
            if (itinerary == null) return;

            currentTripId = itinerary.getId(); // keep cached for the launcher callback

            showItinerary();
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDeleteItemSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                viewModel.getDeleteItemSuccess().setValue(false);
            }
        });

        viewModel.getUnscheduleItemSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Item unscheduled", Toast.LENGTH_SHORT).show();
                viewModel.getUnscheduleItemSuccess().setValue(false);
            }
        });
    }

    private void setupListeners() {

    }

    private void showItinerary() {
        viewModel.getFlattenedItinerary().observe(getViewLifecycleOwner(), displayItems -> {
            if (displayItems != null) {
                adapter.setData(displayItems, new ItineraryAdapter.OnItineraryItemClickListener() {
                    @Override
                    public void onItemClicked(ItineraryItem item) {
                        navigateToLocationDetail(item);
                    }

                    @Override
                    public void onDeleteClick(ItineraryItem item) {
                        confirmAndDelete(item);
                    }

                    @Override
                    public void onUnscheduleClick(ItineraryItem item) {
                        if (currentTripId != null)
                            viewModel.unscheduleItineraryItem(currentTripId, item.getId());
                    }

                    @Override
                    public void onAddLocationClick(Date date) {
                        LocationSelectionSheet bottomSheet = new LocationSelectionSheet(currentTripId, date);
                        bottomSheet.show(getChildFragmentManager(), "LocationSelectionSheet");
                    }
                });
            }
        });
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

    private void navigateToLocationDetail(ItineraryItem item) {

    }
}
