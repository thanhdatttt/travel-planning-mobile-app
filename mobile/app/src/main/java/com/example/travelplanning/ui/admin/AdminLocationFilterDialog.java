package com.example.travelplanning.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.R;
import com.example.travelplanning.viewmodel.admin.AdminLocationViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class AdminLocationFilterDialog extends BottomSheetDialogFragment {
    private AdminLocationViewModel viewModel;
    private String currentSortBy;
    private String currentSortOrder;
    private int currentMinPrice;
    private int currentMaxPrice;
    private int currentMinRating;
    private int currentMaxRating;
    private boolean isDeleted = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_filter_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(AdminLocationViewModel.class);

        currentSortBy = viewModel.getSortBy();
        currentSortOrder = viewModel.getSortOrder();
        currentMinPrice = viewModel.getMinPrice();
        currentMaxPrice = viewModel.getMaxPrice();
        currentMinRating = viewModel.getMinRating();
        currentMaxRating = viewModel.getMaxRating();
        isDeleted = viewModel.isDeleted();

        setupDropdowns(view);
        syncChipWithViewmodel(view);

        view.findViewById(R.id.btnReset).setOnClickListener(v -> {
            viewModel.resetFilters();
            dismiss();
        });

        // CLICK FILTER BUTTON
        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            List<String> categoryId = new ArrayList<>();

            if (((Chip) view.findViewById(R.id.chipRestaurant)).isChecked()) categoryId.add("1");
            if (((Chip) view.findViewById(R.id.chipHotel)).isChecked()) categoryId.add("2");
            if (((Chip) view.findViewById(R.id.chipAttraction)).isChecked()) categoryId.add("3");
            if (((Chip) view.findViewById(R.id.chipShopping)).isChecked()) categoryId.add("4");
            if (((Chip) view.findViewById(R.id.chipService)).isChecked()) categoryId.add("5");
            if (((Chip) view.findViewById(R.id.chipDeletedLocation)).isChecked()) isDeleted = true;
            else isDeleted = false;

            viewModel.applyFilters(currentMinPrice, currentMaxPrice, currentMinRating, currentMaxRating, categoryId, currentSortBy, currentSortOrder, isDeleted);
            dismiss();
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
    }

    private void syncChipWithViewmodel(View v) {
        List<String> categoryId = viewModel.getCategoryId();
        ((Chip) v.findViewById(R.id.chipRestaurant)).setChecked(categoryId.contains("1"));
        ((Chip) v.findViewById(R.id.chipHotel)).setChecked(categoryId.contains("2"));
        (((Chip) v.findViewById(R.id.chipAttraction))).setChecked(categoryId.contains("3"));
        ((Chip) v.findViewById(R.id.chipShopping)).setChecked(categoryId.contains("4"));
        ((Chip) v.findViewById(R.id.chipService)).setChecked(categoryId.contains("5"));
        ((Chip) v.findViewById(R.id.chipDeletedLocation)).setChecked(isDeleted);
    }

    private void setupDropdowns(View v) {
        AutoCompleteTextView actvBy = v.findViewById(R.id.actvSortBy);
        AutoCompleteTextView actvOrder = v.findViewById(R.id.actvSortOrder);
        AutoCompleteTextView actvMinPrice = v.findViewById(R.id.actvMinPrice);
        AutoCompleteTextView actvMaxPrice = v.findViewById(R.id.actvMaxPrice);
        AutoCompleteTextView actvMinRating = v.findViewById(R.id.actvMinRating);
        AutoCompleteTextView actvMaxRating = v.findViewById(R.id.actvMaxRating);


        String[] byOptions = {"name", "price", "rating"};
        String[] byOptionsDB = {"name", "priceLevel", "avgRating"};
        String[] orderOptions = {"asc", "desc"};
        String[] priceOptions = {"$", "$$", "$$$", "$$$$"};
        String[] ratingOptions = {"0", "1", "2", "3", "4", "5"};

        actvBy.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, byOptions));
        actvOrder.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, orderOptions));
        actvMinPrice.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, priceOptions));
        actvMaxPrice.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, priceOptions));
        actvMinRating.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, ratingOptions));
        actvMaxRating.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, ratingOptions));

        if(currentSortBy.equals("avgRating"))   currentSortBy = "rating";
        if(currentSortBy.equals("priceLevel"))  currentSortBy = "price";
        actvBy.setText(currentSortBy, false);
        actvOrder.setText(currentSortOrder, false);
        actvMinPrice.setText("$".repeat(currentMinPrice), false);
        actvMaxPrice.setText("$".repeat(currentMaxPrice), false);
        actvMinRating.setText(String.valueOf(currentMinRating), false);
        actvMaxRating.setText(String.valueOf(currentMaxRating), false);

        actvBy.setOnItemClickListener((p, view1, pos, id) -> currentSortBy = byOptionsDB[pos]);
        actvOrder.setOnItemClickListener((p, view1, pos, id) -> currentSortOrder = orderOptions[pos]);
        actvMinPrice.setOnItemClickListener((p, view1, pos, id) -> currentMinPrice = pos+1);
        actvMaxPrice.setOnItemClickListener((p, view1, pos, id) -> currentMaxPrice = pos+1);
        actvMinRating.setOnItemClickListener((p, view1, pos, id) -> currentMinRating = pos);
        actvMaxRating.setOnItemClickListener((p, view1, pos, id) -> currentMaxRating = pos);

    }
}