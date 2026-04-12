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
import com.example.travelplanning.databinding.FragmentTripPublicLocationListBinding;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;

import java.util.List;

public class TripPublicLocationListFragment extends Fragment {
    private FragmentTripPublicLocationListBinding binding;
    private ItineraryViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTripPublicLocationListBinding.inflate(inflater, container, false);
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

    }
}
