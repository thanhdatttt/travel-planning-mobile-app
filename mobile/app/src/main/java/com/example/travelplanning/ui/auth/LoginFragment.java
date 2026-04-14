package com.example.travelplanning.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.FragmentLoginBinding;
import com.example.travelplanning.ui.mainscreen.MainScreenActivity;
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.loginLayout.setVisibility(loading ? View.GONE : View.VISIBLE);
            binding.btnLogin.setEnabled(!loading);
        });


        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) SnackBarHelper.showTopSnackBar(binding.getRoot(), msg, SnackBarHelper.SnackBarType.ERROR);
        });


        viewModel.getLoginSuccess().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                startActivity(new Intent(requireActivity(), MainScreenActivity.class));
                SnackBarHelper.showTopSnackBar(binding.getRoot(), "Login successful!", SnackBarHelper.SnackBarType.SUCCESS);
                requireActivity().finish();
            }
        });

        viewModel.getSocialLoginSuccess().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                startActivity(new Intent(requireActivity(), MainScreenActivity.class));
                SnackBarHelper.showTopSnackBar(binding.getRoot(), "Login successful!", SnackBarHelper.SnackBarType.SUCCESS);
                requireActivity().finish();
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtLoginUsername.getText().toString().trim();
            String pass = binding.edtLoginPassword.getText().toString().trim();
            if (email.isEmpty()) {
                binding.edtLoginUsername.setError("Email or Username is required");
            }
            if (pass.isEmpty()) {
                binding.edtLoginPassword.setError("Password is required");
            }
            viewModel.login(email, pass);
        });

        binding.tvRegister.setOnClickListener(v -> {
            ((AuthActivity) requireActivity()).navigateTo(new RegisterEmailFragment(), true);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            ((AuthActivity) requireActivity()).navigateTo(new ResetEmailVerificationFragment(), true);
        });

        binding.btnGoogle.setOnClickListener(v -> ((AuthActivity) requireActivity()).triggerGoogleLogin());
        binding.btnFacebook.setOnClickListener(v -> ((AuthActivity) requireActivity()).triggerFacebookLogin());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // avoid memory leak
    }
}
