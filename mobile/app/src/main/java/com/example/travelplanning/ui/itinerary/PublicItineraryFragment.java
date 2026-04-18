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
import com.example.travelplanning.data.model.itinerary.ItineraryItem;
import com.example.travelplanning.databinding.FragmentPublicItineraryBinding;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Date;

public class PublicItineraryFragment extends Fragment {
    private FragmentPublicItineraryBinding binding;
    private ItineraryViewModel viewModel;
    private final PublicItineraryAdapter adapter = new PublicItineraryAdapter();
    private String currentTripId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPublicItineraryBinding.inflate(inflater, container, false);
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
        });

        showItinerary();

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {

    }

    private void showItinerary() {
        viewModel.getFlattenedItinerary().observe(getViewLifecycleOwner(), displayItems -> {
            if (displayItems != null) {
                adapter.setData(displayItems, new PublicItineraryAdapter.OnItineraryItemClickListener() {
                    @Override
                    public void onItemClicked(ItineraryItem item) {
                        navigateToLocationDetail(item);
                    }
                });
            }
        });
    }

    private void navigateToLocationDetail(ItineraryItem item) {

    }
}
