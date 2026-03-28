package com.example.travelplanning.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.viewmodel.admin.AdminViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminFilterDialog extends BottomSheetDialogFragment {
    private AdminViewModel viewModel;
    private String currentSortBy = "username";
    private String currentSortOrder = "asc";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_filter_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireParentFragment()).get(AdminViewModel.class);

        syncUIWithViewModel(view);

        setupDropdowns(view);

        view.findViewById(R.id.btnReset).setOnClickListener(v -> {
            viewModel.resetFilters();
            dismiss();
        });

        // Nút Filter: Gom dữ liệu và đẩy về ViewModel
        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            List<UserRole> roles = new ArrayList<>();
            if (((Chip) view.findViewById(R.id.chipAdmin)).isChecked()) roles.add(UserRole.ADMIN);
            if (((Chip) view.findViewById(R.id.chipModerator)).isChecked()) roles.add(UserRole.MODERATOR);
            if (((Chip) view.findViewById(R.id.chipNormalUser)).isChecked()) roles.add(UserRole.USER);

            boolean isBanned = ((Chip) view.findViewById(R.id.chipBanned)).isChecked();
            boolean isInactive = ((Chip) view.findViewById(R.id.chipInactive)).isChecked();
            boolean isDeleted = ((Chip) view.findViewById(R.id.chipDeleted)).isChecked();

            viewModel.applyFilters(isBanned, isDeleted, isInactive, currentSortBy, currentSortOrder, roles);
            dismiss();
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());
    }

    private void syncUIWithViewModel(View v) {
        ((Chip) v.findViewById(R.id.chipBanned)).setChecked(viewModel.isBanned());
        ((Chip) v.findViewById(R.id.chipInactive)).setChecked(viewModel.isInactive());
        ((Chip) v.findViewById(R.id.chipDeleted)).setChecked(viewModel.isDeleted());

        List<UserRole> roles = viewModel.getSelectedRoles();
        ((Chip) v.findViewById(R.id.chipAdmin)).setChecked(roles.contains(UserRole.ADMIN));
        ((Chip) v.findViewById(R.id.chipModerator)).setChecked(roles.contains(UserRole.MODERATOR));
        ((Chip) v.findViewById(R.id.chipNormalUser)).setChecked(roles.contains(UserRole.USER));

        currentSortBy = viewModel.getSortBy();
        currentSortOrder = viewModel.getSortOrder();
    }

    private void setupDropdowns(View v) {
        AutoCompleteTextView actvBy = v.findViewById(R.id.actvSortBy);
        AutoCompleteTextView actvOrder = v.findViewById(R.id.actvSortOrder);

        String[] byOptions = {"username", "email", "created_at"};
        String[] orderOptions = {"asc", "desc"};

        actvBy.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, byOptions));
        actvOrder.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, orderOptions));

        actvBy.setText(currentSortBy, false);
        actvOrder.setText(currentSortOrder, false);

        actvBy.setOnItemClickListener((parent, view, position, id) -> currentSortBy = byOptions[position]);
        actvOrder.setOnItemClickListener((parent, view, position, id) -> currentSortOrder = orderOptions[position]);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}