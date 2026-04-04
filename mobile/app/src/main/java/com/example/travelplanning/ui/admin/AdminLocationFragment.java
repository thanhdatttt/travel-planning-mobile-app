package com.example.travelplanning.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.databinding.FragmentAdminLocationBinding;
import com.example.travelplanning.databinding.SearchAndFilterBinding;
import com.example.travelplanning.databinding.AdminHeaderBinding;
import com.example.travelplanning.viewmodel.admin.AdminLocationViewModel;

import java.util.ArrayList;
import java.util.List;

public class AdminLocationFragment extends Fragment {
    private FragmentAdminLocationBinding binding;
    private SearchAndFilterBinding searchAndFilterBinding;
    private AdminHeaderBinding adminHeaderBinding;
    private AdminLocationViewModel viewModel;
    private AdminLocationAdapter adapter;
    private final List<Location> locationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminLocationBinding.inflate(inflater, container, false);
        searchAndFilterBinding = SearchAndFilterBinding.bind(binding.searchAndFilter.getRoot());
        adminHeaderBinding = AdminHeaderBinding.bind(binding.adminHeader.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminLocationViewModel.class);

        adminHeaderBinding.btnLocation.setSelected(true);
        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.fetchLocations(false);
    }

    private void setupRecyclerView() {
        adapter = new AdminLocationAdapter(locationList, this::showPopupMenu);
        binding.rvLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLocations.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getLocations().observe(getViewLifecycleOwner(), locations -> {
            if (locations != null) {
                locationList.clear();
                locationList.addAll(locations);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        searchAndFilterBinding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onSearchQueryChanged(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchAndFilterBinding.btnFilter.setOnClickListener(v ->
                new AdminLocationFilterDialog().show(getChildFragmentManager(), "AdminLocationFilter"));

        adminHeaderBinding.btnUser.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // SCROLLING LISTENER
        binding.rvLocations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // ONLY FETCH WHEN NEAR BOTTOM OF RC
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0) {
                        viewModel.fetchLocations(true);
                    }
                }
            }
        });
    }

    private void showPopupMenu(View anchor, Location location) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Edit Location");
        popup.getMenu().add(0, 2, 1, "Delete Location");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 2) {
                // viewModel.deleteLocation(location);
                return true;
            }
            return false;
        });
        popup.show();
    }
}