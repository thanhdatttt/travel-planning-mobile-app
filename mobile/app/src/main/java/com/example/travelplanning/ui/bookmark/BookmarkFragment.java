package com.example.travelplanning.ui.bookmark;

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
import com.example.travelplanning.databinding.FragmentBookmarkListBinding;
import com.example.travelplanning.ui.map.LocationAdapter;
import com.example.travelplanning.viewmodel.bookmark.BookmarkViewModel;

public class BookmarkFragment extends Fragment {
    private FragmentBookmarkListBinding binding;
    private BookmarkViewModel viewModel;
    private LocationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookmarkListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);

        setupRecyclerView();
        setupObservers();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        // Gọi API lấy dữ liệu trang 1
        viewModel.fetchAllBookmarks(1);
    }

    private void setupRecyclerView() {
        adapter = new LocationAdapter(location -> {
            Bundle bundle = new Bundle();
            bundle.putString("location_id", location.getId());
            Navigation.findNavController(requireView()).navigate(R.id.nav_location_detail, bundle);
        });

        binding.rvBookmarks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBookmarks.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getBookmarkedLocations().observe(getViewLifecycleOwner(), locations -> {
            binding.pbLoading.setVisibility(View.GONE);

            if (locations != null && !locations.isEmpty()) {
                adapter.updateData(locations, null);
                binding.rvBookmarks.setVisibility(View.VISIBLE);
                binding.tvEmptyMessage.setVisibility(View.GONE);
            } else {
                // Trường hợp danh sách rỗng
                binding.rvBookmarks.setVisibility(View.GONE);
                binding.tvEmptyMessage.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.pbLoading.setVisibility(View.VISIBLE);
                binding.tvEmptyMessage.setVisibility(View.GONE);
            } else {
                binding.pbLoading.setVisibility(View.GONE);
            }
        });
    }
}