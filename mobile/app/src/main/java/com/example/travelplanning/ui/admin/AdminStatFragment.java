package com.example.travelplanning.ui.admin;

import android.graphics.Color;
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
import androidx.recyclerview.widget.RecyclerView;
import java.util.function.Consumer;

import com.example.travelplanning.R;
import com.example.travelplanning.data.mapper.admin.AdminStatMapper;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.databinding.AdminHeaderBinding;
import com.example.travelplanning.databinding.FragmentAdminStatBinding;
import com.example.travelplanning.viewmodel.admin.AdminStatViewModel;
import com.example.travelplanning.viewmodel.admin.AdminUserViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;
import java.util.Collections;

public class AdminStatFragment extends Fragment {
    private FragmentAdminStatBinding binding;
    private AdminHeaderBinding adminHeaderBinding;
    private AdminStatViewModel viewModel;
    private AdminUserViewModel userViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminStatBinding.inflate(inflater, container, false);
        adminHeaderBinding = AdminHeaderBinding.bind(binding.adminHeader.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminStatViewModel.class);
        userViewModel = new ViewModelProvider(this).get(AdminUserViewModel.class);


        adminHeaderBinding.btnChart.setSelected(true);
        setupListeners();
        setupObservers();

        viewModel.fetchStats();
    }

    private void setupObservers() {
        viewModel.getSelectedMonth().observe(getViewLifecycleOwner(), m ->
                binding.tvMonthPicker.setText(String.valueOf(m)));

        viewModel.getSelectedYear().observe(getViewLifecycleOwner(), y ->
                binding.tvYearPicker.setText(String.valueOf(y)));

        viewModel.getStats().observe(getViewLifecycleOwner(), response -> {
            if (response == null) return;
            binding.chartSignIn.setData(AdminStatMapper.mapToFloatList(response.getSignInData()), Color.parseColor("#4CAF50"));
            binding.chartReviews.setData(AdminStatMapper.mapToFloatList(response.getReviewData()), Color.parseColor("#2196F3"));

            if (response.getCounts() != null) {
                binding.tvUser.setText(String.valueOf(response.getCounts().getUser()));
                binding.tvAdmin.setText(String.valueOf(response.getCounts().getAdmin()));
                if (binding.tvModerator instanceof android.widget.TextView) {
                    ((android.widget.TextView) binding.tvModerator).setText(String.valueOf(response.getCounts().getModerator()));
                }
            }
        });
    }

    private void setupListeners() {
        binding.tvMonthPicker.setOnClickListener(v ->
                showListPopup(v, viewModel.getMonthList(), (val) ->
                        viewModel.setDate(Integer.parseInt(val), viewModel.getSelectedYear().getValue())));

        binding.tvYearPicker.setOnClickListener(v ->
                showListPopup(v, viewModel.getYearList(), (val) ->
                        viewModel.setDate(viewModel.getSelectedMonth().getValue(), Integer.parseInt(val))));

        binding.LLUserUser.setOnClickListener(v -> showUserListDialog(UserRole.USER));
        binding.LLModeratorUser.setOnClickListener(v -> showUserListDialog(UserRole.MODERATOR));
        binding.LLAdminUser.setOnClickListener(v -> showUserListDialog(UserRole.ADMIN));

        adminHeaderBinding.btnUser.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_admin));
        adminHeaderBinding.btnLocation.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_admin_location));
    }

    private void showListPopup(View anchor, String[] data, Consumer<String> listener) {
        androidx.appcompat.widget.ListPopupWindow popup = new androidx.appcompat.widget.ListPopupWindow(requireContext());
        popup.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), R.layout.item_dropdown_simple, data));
        popup.setAnchorView(anchor);
        popup.setModal(true);

        popup.setOnItemClickListener((parent, view, position, id) -> {
            listener.accept(data[position]);
            popup.dismiss();
        });

        popup.show();
    }

    private void showUserListDialog(UserRole role) {
        // Tạo RecyclerView cho Dialog
        RecyclerView recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Sử dụng Adapter mới
        AdminRoleUserAdapter adapter = new AdminRoleUserAdapter();
        recyclerView.setAdapter(adapter);

        // Hiển thị Dialog
        new MaterialAlertDialogBuilder(requireContext(), R.style.WhiteDialog)
                .setTitle("List of " + role.name())
                .setView(recyclerView)
                .setPositiveButton("Close", null)
                .show();

        // Observe dữ liệu người dùng
        userViewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) adapter.setData(users);
        });

        // Gọi API lọc theo Role
        userViewModel.applyFilters(false, false, false, "username", "asc", Collections.singletonList(role));
    }
}