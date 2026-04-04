package com.example.travelplanning.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.databinding.FragmentAdminUserBinding;
import com.example.travelplanning.databinding.SearchAndFilterBinding;
import com.example.travelplanning.databinding.AdminHeaderBinding;
import com.example.travelplanning.viewmodel.admin.AdminUserViewModel;

import java.util.ArrayList;
import java.util.List;

public class AdminUserFragment extends Fragment {
    private FragmentAdminUserBinding binding;
    private SearchAndFilterBinding searchAndFilterBinding;
    private AdminHeaderBinding adminHeaderBinding;
    private AdminUserViewModel viewModel;
    private AdminUserAdapter adapter;
    private List<UserProfile> userList = new ArrayList<>();

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
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {

        });

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
            if (getActivity() != null) getActivity();
        });

        searchAndFilterBinding.btnFilter.setOnClickListener(v -> {
            new AdminUserFilterDialog().show(getChildFragmentManager(), "AdminFilterDialog");
        });

        adminHeaderBinding.btnLocation.setOnClickListener(v -> {
            adminHeaderBinding.btnUser.setSelected(false);
            Navigation.findNavController(v).navigate(R.id.nav_admin_location);
        });
    }

    private void showPopupMenu(View anchor, UserProfile user) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);

        popup.getMenu().add(0, 1, 0, Boolean.TRUE.equals(user.getIsBanned()) ? "Unban user" : "Ban user");
        popup.getMenu().add(0, 2, 1, Boolean.TRUE.equals(user.getIsDeleted()) ? "Restore user" : "Delete user");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                viewModel.toggleBanStatus(user);
                return true;
            } else if (id == 2) {
                viewModel.toggleSoftDelete(user);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void appendBold(SpannableStringBuilder sb, String title, String value) {
        int start = sb.length();
        sb.append(title);
        // Apply BOLD style only to the 'title' part
        sb.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                start, sb.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(value).append("\n");
    }
    private void showUserInfoDialog(UserProfile user) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext());

        // Build a simple but clean summary string
        SpannableStringBuilder info = new SpannableStringBuilder();
        appendBold(info, "ID: ", user.getId() != null ? user.getId() : "N/A");
        info.append("\n");

        appendBold(info, "Full name: ", user.getFullName());
        appendBold(info, "Email: ", user.getEmail());
        appendBold(info, "Address: ", user.getAddress() != null ? user.getAddress() : "N/A");
        appendBold(info, "Phone: ", user.getPhone() != null ? user.getPhone() : "N/A");
        appendBold(info, "Date of birth: ", user.getDob() != null ? user.getDob().toString() : "N/A");
        appendBold(info, "Role: ", String.valueOf(user.getRole()));
        info.append("\n");

        appendBold(info, "Banned: ", Boolean.TRUE.equals(user.getIsBanned()) ? "Yes" : "No");
        appendBold(info, "Deleted: ", Boolean.TRUE.equals(user.getIsDeleted()) ? "Yes" : "No");

        builder.setTitle(user.getUsername() + "'s Information")
                .setMessage(info)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.ic_user) // Ensure you have a user icon
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}