package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.databinding.FragmentTripLocationListBinding;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class TripLocationListFragment extends Fragment {
    private FragmentTripLocationListBinding binding;
    private ItineraryViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        binding = null; // avoid memory leak
    }

    private void setupRecyclerView() {
        binding.rvLocations.setLayoutManager(new LinearLayoutManager(requireContext()));
//        binding.rvLocations.setAdapter();
    }

    private void setupObservers() {
        viewModel.getSelectedItinerary().observe(getViewLifecycleOwner(), itinerary -> {
            if (itinerary != null && itinerary.getItineraryItems() != null) {
                List<ItineraryItem> items = itinerary.getItineraryItems();

                String locationText = items.size() + (items.size() <= 1 ? " location" : " locations");
                binding.tvLocationCount.setText(locationText);
            }
        });
    }

    private void setupListeners() {
        binding.btnAddLocation.setOnClickListener(v -> {
            // add location search
            Snackbar.make(binding.getRoot(), "Add location search not implemented yet", Snackbar.LENGTH_SHORT).show();
        });
    }
}
