package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.FragmentTripPublicDetailBinding;
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TripPublicDetailFragment extends Fragment {
    private FragmentTripPublicDetailBinding binding;
    private ItineraryViewModel viewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // newInstance to pass data to fragment
    private String tripId;

    public static TripPublicDetailFragment newInstance(String id) {
        TripPublicDetailFragment fragment = new TripPublicDetailFragment();
        Bundle args = new Bundle();
        args.putString("arg_public_trip_id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tripId = getArguments().getString("arg_public_trip_id");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTripPublicDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);

        if (tripId != null) {
            viewModel.fetchItineraryById(tripId);
        }

        setupTabUI();
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

        viewModel.getCloneSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                SnackBarHelper.showTopSnackBar(binding.getRoot(), "Trip cloned successfully!", SnackBarHelper.SnackBarType.SUCCESS);
                viewModel.getCloneSuccess().setValue(false);
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnCloneTrip.setOnClickListener(v -> {
            viewModel.cloneItinerary(tripId);
        });
    }

    private void setupTabUI() {
        TripPublicPagerAdapter adapter = new TripPublicPagerAdapter(this);
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
        if (itinerary.getItineraryItems() != null && !itinerary.getItineraryItems().isEmpty()) {
            binding.tvTripLocation.setText(itinerary.getItineraryItems().get(0).getLocation().getName());
            Glide.with(this).load(itinerary.getItineraryItems().get(0).getLocation().getImageUrl()).into(binding.ivTripCover);
        } else {
            binding.ivTripCover.setImageResource(R.drawable.ic_placeholder);
        }

        // creator info
        binding.tvCreatorName.setText(itinerary.getUser().getUsername());
        if (itinerary.getUser().getAvatarUrl() != null) {
            Glide.with(this).load(itinerary.getUser().getAvatarUrl()).into(binding.ivCreatorAvatar);
        } else {
            binding.ivCreatorAvatar.setImageResource(R.drawable.ic_user);
        }

        binding.tvTripTitle.setText(itinerary.getTitle());
        String dates = dateFormat.format(itinerary.getStartDate()) + " – " + dateFormat.format(itinerary.getEndDate());
        binding.tvTripDates.setText(dates);
        if (itinerary.getDescription() != null) binding.tvTripDescription.setText(itinerary.getDescription());
    }
}
