package com.example.travelplanning.ui.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.R;
import com.example.travelplanning.data.repository.favorite.PaginationScrollListener;
import com.example.travelplanning.databinding.FragmentBookmarkListBinding;
import com.example.travelplanning.databinding.FragmentFavoriteListBinding;
import com.example.travelplanning.ui.itinerary.TripAdapter;
import com.example.travelplanning.viewmodel.favorite.FavoriteViewModel;

import java.util.ArrayList;

public class FavoriteFragment extends Fragment {
    private FavoriteViewModel viewModel;
    private TripAdapter adapter;

    private FragmentFavoriteListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);

        viewModel.fetchCurrentUserId();

        adapter = new TripAdapter(
                itinerary -> {
                    Bundle bundle = new Bundle();
                    String currentUserId = viewModel.getCurrentUserId();

                    if (currentUserId != null && currentUserId.equals(itinerary.getOwnerId())) {
                        bundle.putString("arg_trip_id", itinerary.getId());
                        Navigation.findNavController(view).navigate(R.id.nav_trip_detail, bundle);
                    } else {
                        bundle.putString("arg_public_trip_id", itinerary.getId());
                        Navigation.findNavController(view).navigate(R.id.nav_public_trip_detail, bundle);
                    }
                },
                itinerary -> {
                }
        );

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvFavorites.setLayoutManager(layoutManager);
        binding.rvFavorites.setAdapter(adapter);

        binding.rvFavorites.addOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                viewModel.loadNextPage();
            }

            @Override
            public boolean isLastPage() {
                Integer lastPage = viewModel.getLastPage().getValue();
                return viewModel.getCurrentPage() >= (lastPage != null ? lastPage : 1);
            }

            @Override
            public boolean isLoading() {
                Boolean loading = viewModel.getIsLoading().getValue();
                return loading != null && loading;
            }
        });

        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.fetchFavorites(true));

        viewModel.getFavoriteTrips().observe(getViewLifecycleOwner(), trips -> {
            binding.swipeRefresh.setRefreshing(false);
            adapter.submitList(new ArrayList<>(trips));
            binding.tvEmpty.setVisibility(trips.isEmpty() ? View.VISIBLE : View.GONE);
        });

        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        binding.tvToolbarTitle.setText(R.string.favorite_trips_title);
        binding.tvEmpty.setText(R.string.empty_favorite_message);

        viewModel.fetchFavorites(true);
    }
}
