package com.example.travelplanning.ui.review;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.example.travelplanning.databinding.FragmentMyReviewsBinding;
import com.example.travelplanning.viewmodel.review.MyReviewViewModel;

public class MyReviewFragment extends Fragment {
    private FragmentMyReviewsBinding binding;
    private MyReviewViewModel viewModel;
    private MyReviewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyReviewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MyReviewViewModel.class);

        adapter = new MyReviewAdapter(locationId -> {
            Bundle bundle = new Bundle();
            bundle.putString("location_id", locationId);
            Navigation.findNavController(requireView())
                    .navigate(R.id.nav_location_detail, bundle);
        });

        binding.rvMyReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMyReviews.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        binding.btnLoadMoreReviews.setOnClickListener(v -> {
            viewModel.fetchMyReviews(true);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getReviews().observe(getViewLifecycleOwner(), reviews -> {
            adapter.setReviews(reviews);
            binding.tvEmptyMessage.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.fetchMyReviews(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}