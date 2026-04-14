package com.example.travelplanning.ui.mainscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.FragmentHomeBinding;
import com.example.travelplanning.ui.itinerary.TripPublicAdapter;
import com.example.travelplanning.ui.itinerary.TripPublicDetailFragment;
import com.example.travelplanning.ui.location.LocationSearchActivity; // Đảm bảo import đúng package của bạn
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    // public trip management
    private ItineraryViewModel itineraryViewModel;
    private final TripPublicAdapter publicTripAdapter = new TripPublicAdapter(this::navigateToTripDetail);

    // page control
    private int currentPage = 1;
    private boolean isLoadingMore = false;
    private boolean isLastPage = false;
    private static final int LIMIT = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // init shared itinerary view model
        itineraryViewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        // fetch itinerary init data
        if (itineraryViewModel.getPublicItineraries().getValue() == null || itineraryViewModel.getPublicItineraries().getValue().isEmpty()) {
            currentPage = 1;
            isLastPage = false;
            itineraryViewModel.fetchPublicItineraries(currentPage, LIMIT);
        } else {
            // Reset current page based on fetched data
            currentPage = (int) Math.ceil((double) itineraryViewModel.getPublicItineraries().getValue().size() / LIMIT);
        }

        // Restore nav bar when user presses back from public detail
        requireActivity().getSupportFragmentManager()
                .addOnBackStackChangedListener(() -> {
                    Fragment top = requireActivity().getSupportFragmentManager()
                            .findFragmentById(R.id.nav_host_fragment);
                    if (getActivity() instanceof MainScreenActivity) {
                        if (top instanceof TripPublicDetailFragment) {
                            ((MainScreenActivity) getActivity()).setBottomNavVisibility(View.GONE);
                        } else {
                            ((MainScreenActivity) getActivity()).setBottomNavVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        binding.rvCommunityFeed.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCommunityFeed.setAdapter(publicTripAdapter);
        binding.rvCommunityFeed.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null && !isLoadingMore && !isLastPage) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

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
        itineraryViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        itineraryViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                SnackBarHelper.showTopSnackBar(binding.getRoot(), msg, SnackBarHelper.SnackBarType.ERROR);
        });

        itineraryViewModel.getPublicItineraries().observe(getViewLifecycleOwner(), itineraries -> {
            if (itineraries != null) {
                publicTripAdapter.submitList(itineraries);

                // handle page control
                isLoadingMore = false;
                if (itineraries.size() < (currentPage * LIMIT)) {
                    isLastPage = true;
                }
            }
        });
    }

    private void setupListeners() {
        // Bắt sự kiện click vào thanh tìm kiếm giả
        binding.tvDummySearch.setOnClickListener(v -> {
            // Chuyển sang màn hình tìm kiếm thực sự
            Intent intent = new Intent(requireActivity(), LocationSearchActivity.class);
            startActivity(intent);

            // Thêm hiệu ứng chuyển màn hình mượt mà (Fade in)
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void loadMoreData() {
        isLoadingMore = true;
        if (!isLastPage) {
            currentPage++;
            itineraryViewModel.fetchPublicItineraries(currentPage, LIMIT);
        }
    }

    private void navigateToTripDetail(Itinerary itinerary) {
        // Hide nav bar before navigating
        if (getActivity() instanceof MainScreenActivity) {
            ((MainScreenActivity) getActivity()).setBottomNavVisibility(View.GONE);
        }
        
        TripPublicDetailFragment detailFragment = TripPublicDetailFragment.newInstance(itinerary.getId());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_host_fragment, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}