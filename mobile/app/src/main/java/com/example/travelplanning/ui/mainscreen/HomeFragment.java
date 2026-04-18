package com.example.travelplanning.ui.mainscreen;
import com.example.travelplanning.R;
import androidx.fragment.app.FragmentManager;
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
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.FragmentHomeBinding;
import com.example.travelplanning.ui.itinerary.TripPublicAdapter;
import com.example.travelplanning.ui.itinerary.TripPublicDetailFragment;
import com.example.travelplanning.ui.location.LocationSearchActivity;
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;

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

    private final FragmentManager.OnBackStackChangedListener backStackListener = () -> {
        if (getActivity() == null) return;
        Fragment top = requireActivity().getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (getActivity() instanceof MainScreenActivity) {
            if (top instanceof TripPublicDetailFragment) {
                ((MainScreenActivity) getActivity()).setBottomNavVisibility(View.GONE);
            } else {
                ((MainScreenActivity) getActivity()).setBottomNavVisibility(View.VISIBLE);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itineraryViewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        if (itineraryViewModel.getPublicItineraries().getValue() == null || itineraryViewModel.getPublicItineraries().getValue().isEmpty()) {
            currentPage = 1;
            isLastPage = false;
            itineraryViewModel.fetchPublicItineraries(currentPage, LIMIT);
        } else {
            currentPage = (int) Math.ceil((double) itineraryViewModel.getPublicItineraries().getValue().size() / LIMIT);
        }

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

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(backStackListener);
    }

    @Override
    public void onDestroyView() {
        if (getActivity() != null) {
            requireActivity().getSupportFragmentManager().removeOnBackStackChangedListener(backStackListener);
        }
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
            if (itineraries != null && isResumed()) { 
                publicTripAdapter.submitList(itineraries);

                isLoadingMore = false;
                if (itineraries.size() < (currentPage * LIMIT)) {
                    isLastPage = true;
                }
            }
        });
    }

    private void setupListeners() {
        binding.tvDummySearch.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), LocationSearchActivity.class);
            startActivity(intent);
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