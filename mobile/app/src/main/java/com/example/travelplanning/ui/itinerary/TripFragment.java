package com.example.travelplanning.ui.itinerary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.FragmentTripsBinding;
import com.example.travelplanning.databinding.LayoutTripOptionsBinding;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TripFragment extends Fragment {
    private FragmentTripsBinding binding;
    private ItineraryViewModel viewModel;
    private final TripAdapter tripAdapter = new TripAdapter(this::navigateToDetail, this::showTripBottomOptionsMenu);

    private boolean isFabExpanded = false;
    // page control
    private int currentPage = 1;
    private boolean isLoadingMore = false;
    private boolean isLastPage = false;
    private static final int LIMIT = 10;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTripsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        // fetch init data
        if (viewModel.getUserItineraries().getValue() == null || viewModel.getUserItineraries().getValue().isEmpty()) {
            currentPage = 1;
            isLastPage = false;
            viewModel.fetchUserItineraries(currentPage, LIMIT);
        } else {
            // Reset current page based on fetched data
            currentPage = (int) Math.ceil((double) viewModel.getUserItineraries().getValue().size() / LIMIT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // prevent memory leaks
    }

    private void setupRecyclerView() {
        binding.includeTripList.rvCreatedTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.includeTripList.rvCreatedTrips.setAdapter(tripAdapter);
        binding.includeTripList.rvCreatedTrips.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // scroll down to load more
                if (dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null && !isLoadingMore && !isLastPage) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        // check if scrolled to the end of list to load more
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0) {
                            loadMoreData();
                        }
                    }
                }
            }
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.nestedScrollView.setVisibility(loading ? View.GONE : View.VISIBLE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getUserItineraries().observe(getViewLifecycleOwner(), itineraries -> {
            if (itineraries == null || itineraries.isEmpty()) {
                showEmptyState();
            } else {
                showTripList(itineraries);
            }
        });
    }

    private void setupListeners() {
        // top bar
        binding.btnCreateTrip.setOnClickListener(v -> navigateToCreate());
        binding.btnAi.setOnClickListener(v -> navigateToAi());

        // fab
        binding.fabMain.setOnClickListener(v -> toggleFabMenu());
        binding.viewOverlay.setOnClickListener(v -> collapseFabMenu());
        binding.fabCreateTrip.setOnClickListener(v -> {
            collapseFabMenu();
            navigateToCreate();
        });
        binding.fabAi.setOnClickListener(v -> {
            collapseFabMenu();
            navigateToAi();
        });
    }

    // show and handle click of empty state
    private void showEmptyState() {
        binding.nestedScrollView.setVisibility(View.GONE);
        binding.includeLayoutEmpty.layoutEmpty.setVisibility(View.VISIBLE);
        binding.includeLayoutEmpty.btnCreateFirstTrip.setOnClickListener(v -> navigateToCreate());
    }

    // show and handle click of trip list
    private void showTripList(List<Itinerary> itineraries) {
        // hide empty state
        binding.nestedScrollView.setVisibility(View.VISIBLE);
        binding.includeLayoutEmpty.layoutEmpty.setVisibility(View.GONE);

        // active card
        Itinerary latest = itineraries.get(0);
        binding.includeActiveTrip.tvTripName.setText(latest.getTitle());
        if (latest.getStartDate() != null && latest.getEndDate() != null) {
            String dates = dateFormat.format(latest.getStartDate()) + " – " + dateFormat.format(latest.getEndDate());
            binding.includeActiveTrip.tvTripDates.setText(dates);
        }
        binding.includeActiveTrip.cardActiveTrip.setOnClickListener(v -> navigateToDetail(latest));

        // other trips
        List<Itinerary> otherTrips = itineraries.size() > 1
                ? itineraries.subList(1, itineraries.size()) : java.util.Collections.emptyList();
        tripAdapter.submitList(otherTrips);

        // handle page control
        isLoadingMore = false;
        if (itineraries.size() < (currentPage * LIMIT)) {
            isLastPage = true;
        }
    }

    // show and handle click of trip bottom options menu
    private void showTripBottomOptionsMenu(Itinerary itinerary) {
        // init bottom sheet
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        // binding menu
        LayoutTripOptionsBinding optionsBinding = LayoutTripOptionsBinding.inflate(LayoutInflater.from(requireContext()));
        dialog.setContentView(optionsBinding.getRoot());

        // set content
        optionsBinding.swPrivacy.setChecked(!Objects.equals(itinerary.getPrivacy(), "private"));

        // handle click
        optionsBinding.swPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.updateItinerary(itinerary.getId(), null, null, isChecked ? "private" : "public", null, null);
            optionsBinding.tvPrivacyStatus.setText(isChecked ? "Public — anyone can see this trip" : "Private — only you can see this trip");
            Snackbar.make(requireView(), "Changed the privacy", Snackbar.LENGTH_LONG).show();
            dialog.dismiss();
        });
        optionsBinding.btnCopyTrip.setOnClickListener(v -> {
            viewModel.cloneItinerary(itinerary.getId());
            Snackbar.make(requireView(), "Cloned trip", Snackbar.LENGTH_LONG).show();
            dialog.dismiss();
        });
        optionsBinding.btnDeleteTrip.setOnClickListener(v -> {
            // alert dialog before delete
            new MaterialAlertDialogBuilder(requireContext())
                    .setIcon(R.drawable.ic_delete)
                    .setTitle("Delete this trip?")
                    .setMessage("Are you sure you want to delete this trip? This cannot be undone")
                    .setCancelable(false) // Ensure user makes a choice, can't dismiss by tapping outside
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .setPositiveButton("Delete", (dialogInterface, i) -> {
                        viewModel.deleteItinerary(itinerary.getId());
                        dialogInterface.dismiss();
                        dialog.dismiss();

                        Snackbar.make(requireView(), "Trip deleted", Snackbar.LENGTH_LONG).show();
                    })
                    .show();
        });

        dialog.show();
    }

    // load more data
    private void loadMoreData() {
        isLoadingMore = true;
        if (!isLastPage) {
            currentPage++;
            viewModel.fetchUserItineraries(currentPage, LIMIT);
        }

    }

    // fab menu
    private void toggleFabMenu() {
        if (isFabExpanded) collapseFabMenu();
        else expandFabMenu();
    }

    private void expandFabMenu() {
        isFabExpanded = true;
        binding.viewOverlay.setVisibility(View.VISIBLE);
        binding.fabCreateTrip.setVisibility(View.VISIBLE);
        binding.fabAi.setVisibility(View.VISIBLE);

        // rotate + icon to x icon
        binding.fabMain.animate().rotation(45f).setDuration(200).start();

        // animations
        animateFabIn(binding.fabCreateTrip);
        animateFabIn(binding.fabAi);
    }

    private void collapseFabMenu() {
        isFabExpanded = false;
        binding.viewOverlay.setVisibility(View.GONE);

        // rotate x icon to + icon
        binding.fabMain.animate().rotation(0f).setDuration(200).start();

        binding.fabCreateTrip.setVisibility(View.GONE);
        binding.fabAi.setVisibility(View.GONE);
    }

    private void animateFabIn(View fab) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(fab, "alpha", (float) 0.0, 1f);
        ObjectAnimator scale = ObjectAnimator.ofFloat(fab, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(fab, "scaleY", 0f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, scale, scaleY);
        set.setDuration(200);
        set.start();
    }

    // navigate
    private void navigateToCreate() {
        ((TripActivity) requireActivity()).navigateTo(new CreateTripFragment(), true);
    }

    private void navigateToDetail(Itinerary itinerary) {
        ((TripActivity) requireActivity()).navigateTo(TripDetailFragment.newInstance(itinerary), true);
    }

    private void navigateToAi() {
        // TODO: wire to your AI screen when ready
        Snackbar.make(requireView(), "AI assistant coming soon", Snackbar.LENGTH_LONG).show();
    }
}
