package com.example.travelplanning.ui.admin;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.databinding.AdminHeaderBinding;
import com.example.travelplanning.databinding.FragmentAdminLocationBinding;
import com.example.travelplanning.databinding.SearchAndFilterBinding;
import com.example.travelplanning.viewmodel.admin.AdminViewModel;

import java.util.ArrayList;
import java.util.List;

public class AdminLocationFragment extends Fragment {
    private FragmentAdminLocationBinding binding;
    private SearchAndFilterBinding searchAndFilterBinding;
    private AdminHeaderBinding adminHeaderBinding;
    private AdminViewModel viewModel;
    private AdminUserAdapter adapter;
    private List<UserProfile> userList = new ArrayList<>();

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
        viewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        adminHeaderBinding.btnLocation.setSelected(true);
        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.fetchUsers();
    }

    private void setupRecyclerView() {
        // Khởi tạo adapter với listener cho nút Option (Ban/Delete...)
        adapter = new AdminUserAdapter(userList, user -> {
            // Xử lý khi nhấn nút Option (Hiện PopupMenu hoặc Toggle Ban trực tiếp)
            viewModel.toggleBanStatus(user);
            Toast.makeText(getContext(), "Toggling ban for: " + user.getUsername(), Toast.LENGTH_SHORT).show();
        });

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUsers.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            // Bạn có thể thêm một ProgressBar vào XML nếu muốn
            // binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
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
        // Lắng nghe ô Search (EditText)
        searchAndFilterBinding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adminHeaderBinding.ivBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        searchAndFilterBinding.btnFilter.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Filter popup coming soon", Toast.LENGTH_SHORT).show();
        });

        adminHeaderBinding.btnUser.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_admin);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}