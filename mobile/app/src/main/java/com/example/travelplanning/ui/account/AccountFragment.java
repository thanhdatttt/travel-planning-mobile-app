package com.example.travelplanning.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.R;
import com.example.travelplanning.databinding.FragmentAccountBinding;
import com.example.travelplanning.viewmodel.account.AccountViewModel;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private AccountViewModel viewModel;
    private AccountListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new AccountListAdapter(this::handleMenuClick);
        binding.rvAccountMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAccountMenu.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        viewModel.getMenuItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
        });
    }

    private void handleMenuClick(AccountOption option) {
        switch (option.getId()) {
            case AccountViewModel.ID_INFO:
                //qua profile
                break;
            case AccountViewModel.ID_LOGOUT:
                //đăng xuất
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}