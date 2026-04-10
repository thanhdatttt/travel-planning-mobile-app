package com.example.travelplanning.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.FragmentRegisterDetailBinding;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class RegisterDetailFragment extends Fragment {
    private FragmentRegisterDetailBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterDetailBinding.inflate(inflater, container, false);
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
            binding.registerLayout.setVisibility(loading ? View.GONE : View.VISIBLE);
            binding.btnCreateAccount.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
        });

        viewModel.getRegisterSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                // return to lo gin and delete all back stack
                requireActivity().getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                ((AuthActivity) requireActivity()).navigateTo(new LoginFragment(), false);

                viewModel.getRegisterSuccess().setValue(null);
            }
        });
    }

    private void setupListeners() {
        binding.btnCreateAccount.setOnClickListener(v -> {
            String username = binding.edtRegisterUser.getText().toString().trim();
            String pass = binding.edtRegisterPass.getText().toString().trim();
            String confirmPass = binding.edtRegisterConfirm.getText().toString().trim();

            if (username.isEmpty()) {
                binding.edtRegisterUser.setError("Username is required");
                return;
            }
            if (pass.isEmpty()) {
                binding.edtRegisterPass.setError("Password is required");
                return;
            }
            if (confirmPass.isEmpty()) {
                binding.edtRegisterConfirm.setError("Please confirm your password");
                return;
            }
            if (!pass.equals(confirmPass)) {
                binding.edtRegisterConfirm.setError("Password does not match");
                return;
            }

            // get state
            String email = viewModel.getCurrentEmail();

            viewModel.register(email, username, pass, confirmPass);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
