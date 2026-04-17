package com.example.travelplanning.ui.account;

import android.content.Intent;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.databinding.FragmentAccountBinding;
import com.example.travelplanning.ui.auth.AuthActivity;
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.account.AccountViewModel;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;
import com.example.travelplanning.viewmodel.profile.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {
    private FragmentAccountBinding binding;
    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rvAccountMenu.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(binding.rvAccountMenu.getContext(), layoutManager.getOrientation());
        binding.rvAccountMenu.addItemDecoration(dividerItemDecoration);

        // 1. Thiết lập Observer để lắng nghe khi có dữ liệu Profile
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            Log.d("ACCOUNT_DEBUG", "Observer triggered!");
            if (user != null) {
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                    Glide.with(this)
                            .load(user.getAvatarUrl())
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .centerCrop()
                            .into(binding.ivAvatar);
                } else {
                    binding.ivAvatar.setImageResource(R.drawable.ic_user);
                }
                // Tạo danh sách menu dựa trên user vừa nhận được
                List<AccountOption> menuItems = getMenuItemsByRole(user);

                // Cập nhật Adapter
                AccountAdapter adapter = new AccountAdapter(menuItems, this::handleMenuClick);
                binding.rvAccountMenu.setAdapter(adapter);
            }
        });

        // 2. Gọi API lấy thông tin Profile
        profileViewModel.fetchUserProfile();

        // logout
        observeLogoutStatus();
        handleLogout();
    }

    private List<AccountOption> getMenuItemsByRole(UserProfile profile) {
        List<AccountOption> list = new ArrayList<>();
        list.add(new AccountOption(AccountViewModel.ID_INFO, R.drawable.ic_user, R.string.personal_info));
        list.add(new AccountOption(AccountViewModel.ID_SETTING, R.drawable.ic_setting, R.string.setting));
        list.add(new AccountOption(AccountViewModel.ID_REVIEW, R.drawable.ic_star_vector, R.string.my_reviews));
        list.add(new AccountOption(AccountViewModel.ID_BOOKMARK, R.drawable.ic_bookmark_full, R.string.saved_location));
        list.add(new AccountOption(AccountViewModel.ID_FAV, R.drawable.ic_heart, R.string.favorite_trips_title));
        //check role then add admin board
        if (profile != null && profile.getRole() == UserRole.ADMIN) {
            list.add(new AccountOption(AccountViewModel.ID_ADMIN, R.drawable.ic_admin, R.string.menu_admin));
        }

        return list;
    }

    private void handleMenuClick(AccountOption option) {
        if (option.getId() == AccountViewModel.ID_INFO) {
            // Sử dụng NavController để điều hướng
            Navigation.findNavController(requireView())
                    .navigate(R.id.nav_profile);
        } else if (option.getId() == AccountViewModel.ID_SETTING) {
            Navigation.findNavController(requireView())
                    .navigate(R.id.nav_settings);
        } else if (option.getId() == AccountViewModel.ID_REVIEW){
            Navigation.findNavController(requireView()).navigate(R.id.nav_review);
        }else if (option.getId() == AccountViewModel.ID_BOOKMARK) {
            Navigation.findNavController(requireView()).navigate(R.id.nav_saved_locations);
        }else if (option.getId() == AccountViewModel.ID_FAV) {
            Navigation.findNavController(requireView()).navigate(R.id.nav_favorite_trips);
        }else if (option.getId() == AccountViewModel.ID_ADMIN){
            Navigation.findNavController(requireView()).navigate(R.id.nav_admin);
        }  else if (option.getId() == AccountViewModel.ID_LOGOUT) {
            // Logout

        }
    }

    // handle logout
    private void observeLogoutStatus() {
        authViewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), isLoggedOut -> {
            if (isLoggedOut) {
                SnackBarHelper.showTopSnackBar(binding.getRoot(), "Logout successfully!", SnackBarHelper.SnackBarType.SUCCESS);
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