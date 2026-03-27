package com.example.travelplanning.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.databinding.FragmentResetPasswordBinding;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;

public class ResetPasswordFragment extends Fragment {
    private FragmentResetPasswordBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupListeners() {
        binding.btnReset.setOnClickListener(v -> {
            String newPassword = binding.edtResetPass.getText().toString().trim();
            String confirmPassword = binding.edtResetConfirm.getText().toString().trim();

            // Lấy email từ ViewModel
            String email = viewModel.getCurrentEmail();

            viewModel.resetPassword(email, newPassword, confirmPassword);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnReset.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getResetPasswordSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                // return to login, delete back stack
                requireActivity().getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                ((AuthActivity) requireActivity()).navigateTo(new LoginFragment(), false);

                viewModel.getResetPasswordSuccess().setValue(false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
