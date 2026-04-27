package com.example.travelplanning.ui.admin;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.databinding.FragmentAdminUserBinding;
import com.example.travelplanning.databinding.SearchAndFilterBinding;
import com.example.travelplanning.databinding.AdminHeaderBinding;
import com.example.travelplanning.viewmodel.admin.AdminUserViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminUserFragment extends Fragment {
    private FragmentAdminUserBinding binding;
    private SearchAndFilterBinding searchAndFilterBinding;
    private AdminHeaderBinding adminHeaderBinding;
    private AdminUserViewModel viewModel;
    private AdminUserAdapter adapter;
    private final List<UserProfile> userList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminUserBinding.inflate(inflater, container, false);
        searchAndFilterBinding = SearchAndFilterBinding.bind(binding.searchAndFilter.getRoot());
        adminHeaderBinding = AdminHeaderBinding.bind(binding.adminHeader.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminUserViewModel.class);

        adminHeaderBinding.btnUser.setSelected(true);
        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.fetchUsers();
    }

    private void setupRecyclerView() {
        adapter = new AdminUserAdapter(userList, this::showPopupMenu, this::showUserInfoDialog);
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUsers.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {});

        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                userList.clear();
                userList.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (Boolean.TRUE.equals(isLoading)) {
                showLoading(getString(R.string.fetching_users));
            } else {
                hideLoading();
            }
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

        adminHeaderBinding.ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        searchAndFilterBinding.btnFilter.setOnClickListener(v -> {
            new AdminUserFilterDialog().show(getChildFragmentManager(), "AdminFilterDialog");
        });

        adminHeaderBinding.btnChart.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_admin_stat);
        });
        adminHeaderBinding.btnLocation.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_admin_location);
        });
    }

    private void showPopupMenu(View anchor, UserProfile user) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);

        popup.getMenu().add(0, 1, 0, Boolean.TRUE.equals(user.getIsBanned()) ? R.string.unban_user : R.string.ban_user);
        popup.getMenu().add(0, 2, 1, Boolean.TRUE.equals(user.getIsDeleted()) ? R.string.restore_user : R.string.delete_user);
        popup.getMenu().add(0, 3, 2, R.string.edit_user);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(Boolean.TRUE.equals(user.getIsBanned()) ? R.string.unban_user : R.string.ban_user))
                        .setMessage(getString(R.string.are_you_sure_you_want_to) + getString(Boolean.TRUE.equals(user.getIsBanned()) ? R.string.unban_user : R.string.ban_user).toLowerCase() + " " + user.getUsername() + "?")
                        .setPositiveButton(getString(R.string.yes), (d, w) -> viewModel.toggleBanStatus(user))
                        .setNegativeButton(getString(R.string.close), null)
                        .show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                return true;
            } else if (id == 2) {
                AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(Boolean.TRUE.equals(user.getIsDeleted()) ? R.string.restore_user : R.string.delete_user))
                        .setMessage(getString(R.string.are_you_sure_you_want_to) + getString(Boolean.TRUE.equals(user.getIsDeleted()) ? R.string.restore_user : R.string.delete_user).toLowerCase() + " " + user.getUsername() + "?")
                        .setPositiveButton(getString(R.string.yes), (d, w) -> viewModel.toggleSoftDelete(user))
                        .setNegativeButton(getString(R.string.close), null)
                        .show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                return true;
            } else if (id == 3){
                showEditUserDialog(user);
            }
            return false;
        });
        popup.show();
    }

    private void showUserInfoDialog(UserProfile user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_info, null);

        // Map the data to the IDs
        TextView tvId = dialogView.findViewById(R.id.tvValueId);
        TextView tvFullName = dialogView.findViewById(R.id.tvValueFullName);
        TextView tvEmail = dialogView.findViewById(R.id.tvValueEmail);
        TextView tvAddress = dialogView.findViewById(R.id.tvValueAddress);
        TextView tvPhone = dialogView.findViewById(R.id.tvValuePhone);
        TextView tvDob = dialogView.findViewById(R.id.tvValueDob);
        TextView tvRole = dialogView.findViewById(R.id.tvValueRole);
        TextView tvBanned = dialogView.findViewById(R.id.tvValueBanned);
        TextView tvDeleted = dialogView.findViewById(R.id.tvValueDeleted);

        tvId.setText(user.getId());
        tvFullName.setText(user.getFullName() != null ? user.getFullName() : "N/A");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
        tvAddress.setText(user.getAddress() != null ? user.getAddress() : "N/A");
        tvPhone.setText(user.getPhone() != null ? user.getPhone() : "N/A");
        tvDob.setText(user.getDob() != null ? user.getDob().toString() : "N/A");

        if (user.getRole() != null){
            if(user.getRole() == UserRole.ADMIN) {
                tvRole.setText(getString(R.string.admin));
            }
            else if (user.getRole() == UserRole.MODERATOR){
                tvRole.setText(getString(R.string.moderator));
            }
            else {
                tvRole.setText(getString(R.string.user));
            }
        }
        else{
            tvRole.setText(getString(R.string.user));
        }

        // Banned Status Color Logic
        boolean isBanned = Boolean.TRUE.equals(user.getIsBanned());
        tvBanned.setText(isBanned ? "Yes" : "No");
        tvBanned.setTextColor(isBanned ? Color.RED : Color.parseColor("#4CAF50"));

        boolean isDeleted = Boolean.TRUE.equals(user.getIsDeleted());
        tvDeleted.setText(isDeleted ? "Yes" : "No");
        tvDeleted.setTextColor(isDeleted ? Color.RED : Color.parseColor("#4CAF50"));

        AlertDialog userDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(user.getUsername() + getString(R.string.s_information))
                .setView(dialogView)
                .setPositiveButton( getString(R.string.close), (dialog, which) -> dialog.dismiss())
                .show();

        userDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green));
    }
    private void showEditUserDialog(UserProfile user) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);

        TextView tvId = view.findViewById(R.id.tvEditId);
        EditText etName = view.findViewById(R.id.etEditFullName);
        EditText etEmail = view.findViewById(R.id.etEditEmail);
        EditText etAddress = view.findViewById(R.id.etEditAddress);
        EditText etPhone = view.findViewById(R.id.etEditPhone);
        EditText etDob = view.findViewById(R.id.etEditDob);
        Spinner spRole = view.findViewById(R.id.spEditRole);

        if (user.getDob() != null) {
            etDob.setText(user.getDob().toString());
        }

        tvId.setText(user.getId());
        etName.setText(user.getFullName() != null ? user.getFullName() : "");
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        etAddress.setText(user.getAddress() != null ? user.getAddress() : "");
        etPhone.setText(user.getPhone() != null ? user.getPhone() : "");

        if (user.getDob() != null) {
            etDob.setText(user.getDob().toString());
        }

        etDob.setOnClickListener(v -> {
            java.time.LocalDate currentDob = user.getDob() != null ? user.getDob() : java.time.LocalDate.now();

            android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(
                    requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        java.time.LocalDate selectedDate = java.time.LocalDate.of(year, month + 1, dayOfMonth);
                        etDob.setText(selectedDate.toString());
                        user.setDob(selectedDate);
                    },
                    currentDob.getYear(),
                    currentDob.getMonthValue() - 1,
                    currentDob.getDayOfMonth()
            );
            datePicker.show();
        });

        UserRole[] roles = {UserRole.USER, UserRole.MODERATOR, UserRole.ADMIN};

        ArrayAdapter<UserRole> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                roles
        );
        spRole.setAdapter(roleAdapter);

        if (user.getRole() != null) {
            for (int i = 0; i < roles.length; i++) {
                if (roles[i] == user.getRole()) {
                    spRole.setSelection(i);
                    break;
                }
            }
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.edit_profile) + user.getUsername())
                .setView(view)
                .setPositiveButton(getString(R.string.edit), (d, which) -> {
                    user.setFullName(etName.getText().toString());
                    user.setEmail(etEmail.getText().toString());
                    user.setAddress(etAddress.getText().toString());
                    user.setPhone(etPhone.getText().toString());

                    UserRole selectedRole = (UserRole) spRole.getSelectedItem();
                    user.setRole(selectedRole);

                    viewModel.editUser(user);
                    Toast.makeText(getContext(), "Updating " + user.getUsername() + "...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), (d, which) -> d.dismiss())
                .show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.dark_green));

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showLoading(String message) {
        if (binding == null) return;
        binding.loadingOverlay.getRoot().setVisibility(View.VISIBLE);
        binding.loadingOverlay.tvLoadingMessage.setText(message);
    }

    private void hideLoading() {
        if (binding == null) return;
        binding.loadingOverlay.getRoot().setVisibility(View.GONE);
    }
}