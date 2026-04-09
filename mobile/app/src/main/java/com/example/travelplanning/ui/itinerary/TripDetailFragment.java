package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.FragmentTripDetailBinding;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.Serializable;

public class TripDetailFragment extends Fragment {
    private static final String ARG_ITINERARY = "arg_itinerary";
    private Itinerary mItinerary;
    private FragmentTripDetailBinding binding;
    private ItineraryViewModel viewModel;

    // newInstance to pass data to fragment
    public static TripDetailFragment newInstance(Itinerary itinerary) {
        TripDetailFragment fragment = new TripDetailFragment();
        Bundle args = new Bundle();

        args.putSerializable(ARG_ITINERARY, (Serializable) itinerary);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItinerary = (Itinerary) getArguments().getSerializable(ARG_ITINERARY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTripDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);

        // fetch detail data of trip
        if (mItinerary != null && mItinerary.getId() != null) {
            viewModel.fetchItineraryById(mItinerary.getId());
        }

        setupTabUI();
        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show();
        });

        viewModel.getSelectedItinerary().observe(getViewLifecycleOwner(), itinerary -> {
            if (itinerary != null) {
                displayTripDetail(itinerary);
            }
        });
    }

    private void setupListeners() {
        binding.btnSettings.setOnClickListener(v -> {
            ((TripActivity) requireActivity()).navigateTo(TripSettingFragment.newInstance(viewModel.getSelectedItinerary().getValue()), true);
        });
        binding.btnBack.setOnClickListener(v -> {
            ((TripActivity) requireActivity()).navigateTo(new TripFragment(), true);
        });
    }

    // setup UI for 2 tab fragments
    private void setupTabUI() {
        TripPagerAdapter adapter = new TripPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // Link TabLayout and ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(R.string.saved_locations);
                    } else {
                        tab.setText(R.string.itinerary);
                    }
                }).attach();
    }

    private void displayTripDetail(Itinerary itinerary) {
        binding.tvTripTitle.setText(itinerary.getTitle());
        binding.tvTripDates.setText(String.format("%s - %s", itinerary.getStartDate(), itinerary.getEndDate()));
        if (itinerary.getDescription() != null) binding.tvTripDescription.setText(itinerary.getDescription());
    }
}
