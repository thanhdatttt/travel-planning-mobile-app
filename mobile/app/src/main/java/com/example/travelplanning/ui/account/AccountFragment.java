package com.example.travelplanning.ui.account;

import android.content.Intent;
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
import com.example.travelplanning.databinding.FragmentAccountBinding;
import com.example.travelplanning.ui.auth.AuthActivity;
import com.example.travelplanning.viewmodel.account.AccountViewModel;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Lấy danh sách menu dựa trên Role (Logic đơn giản)
        List<AccountOption> menuItems = getMenuItemsByRole();

        // 2. Setup Adapter trực tiếp
        AccountAdapter adapter = new AccountAdapter(menuItems, this::handleMenuClick);
        binding.rvAccountMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAccountMenu.setAdapter(adapter);

        // logout
        observeLogoutStatus();
        handleLogout();
    }

    private List<AccountOption> getMenuItemsByRole() {
        List<AccountOption> list = new ArrayList<>();
        list.add(new AccountOption(1, R.drawable.ic_user, R.string.personal_info));
        list.add(new AccountOption(2, R.drawable.ic_setting, R.string.setting));
        list.add(new AccountOption(3, R.drawable.ic_star, R.string.my_reviews));
        list.add(new AccountOption(4, R.drawable.ic_heart, R.string.my_fav_location));
        //check role then add admin board
        // if (user.isAdmin()) {
        //    list.add(new AccountOption(5, R.drawable.ic_admin, R.string.menu_admin));
        // }

        return list;
    }

    private void handleMenuClick(AccountOption option) {
        if (option.getId() == AccountViewModel.ID_INFO) {
            // Sử dụng NavController để điều hướng
            Navigation.findNavController(requireView())
                    .navigate(R.id.nav_profile);
        } else if (option.getId() == AccountViewModel.ID_LOGOUT) {
            // Logout

        }
    }

    // handle logout
    private void observeLogoutStatus() {
        authViewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut) {
                navigateToLogin();
            }
        });
    }

    private void handleLogout() {
        binding.btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}