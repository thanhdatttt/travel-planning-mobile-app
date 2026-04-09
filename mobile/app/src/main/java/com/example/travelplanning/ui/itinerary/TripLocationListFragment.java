package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.FragmentTripLocationListBinding;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;

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

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {

    }

    private void setupListeners() {

    }
}
