package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.FragmentTripDetailBinding;
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TripDetailFragment extends Fragment {
    private FragmentTripDetailBinding binding;
    private ItineraryViewModel viewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // newInstance to pass data to fragment
    private String tripId;

    public static TripDetailFragment newInstance(String id) {
        TripDetailFragment fragment = new TripDetailFragment();
        Bundle args = new Bundle();
        args.putString("arg_trip_id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tripId = getArguments().getString("arg_trip_id");
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
        if (tripId != null) {
            viewModel.fetchItineraryById(tripId);
            viewModel.checkFavoriteStatus(tripId);
        }

        setupTabUI();
        setupFavoriteActions(tripId);
        setupObservers();
        setupListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // prevent memory leaks
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                SnackBarHelper.showTopSnackBar(binding.getRoot(), msg, SnackBarHelper.SnackBarType.ERROR);
        });

        viewModel.getSelectedItinerary().observe(getViewLifecycleOwner(), itinerary -> {
            if (itinerary != null) {
                displayTripDetail(itinerary);
            }
        });
    }

    private void setupListeners() {
        binding.btnSettings.setOnClickListener(v -> {
            assert getParentFragment() != null;
            ((TripContainerFragment) getParentFragment()).navigateTo(TripSettingFragment.newInstance(tripId), true);
        });
        binding.btnBack.setOnClickListener(v -> {
            viewModel.getSelectedItinerary().setValue(null);
            assert getParentFragment() != null;
            ((TripContainerFragment) getParentFragment()).navigateTo(new TripFragment(), false);
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
        String imageUrl = null;
        String firstLocationName = "";

        if (itinerary.getItineraryItems() != null && !itinerary.getItineraryItems().isEmpty()) {
            var firstItem = itinerary.getItineraryItems().get(0);
            if (firstItem != null && firstItem.getLocation() != null) {
                firstLocationName = firstItem.getLocation().getName();
                imageUrl = firstItem.getLocation().getImageUrl();
            }
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder) // image when loading
                    .error(R.drawable.ic_placeholder)       // image when error
                    .centerCrop()
                    .into(binding.ivTripCover);
        } else {
            // clear resource and set default image if source image is empty
            Glide.with(this).clear(binding.ivTripCover);
            binding.ivTripCover.setImageResource(R.drawable.ic_placeholder);
        }

        binding.tvTripLocation.setText(firstLocationName);
        binding.tvTripTitle.setText(itinerary.getTitle());
        String dates = dateFormat.format(itinerary.getStartDate()) + " – " + dateFormat.format(itinerary.getEndDate());
        binding.tvTripDates.setText(dates);
        if (itinerary.getDescription() != null) binding.tvTripDescription.setText(itinerary.getDescription());
    }

    private void setupFavoriteActions(String itineraryId) {
        binding.btnFavorite.setOnClickListener(v -> {
            if (itineraryId != null) {
                viewModel.toggleFavorite(itineraryId);
            }
        });

        viewModel.getIsFavorited().observe(getViewLifecycleOwner(), favorited -> {
            if (favorited) {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart);
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_outline);
            }
        });
    }
}
