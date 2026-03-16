package com.example.travelplanning.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.R;
import com.example.travelplanning.databinding.ActivityResetPasswordBinding;
import com.example.travelplanning.viewmodel.auth.RegisterViewModel;

public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;
    private RegisterViewModel viewModel;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        email = getIntent().getStringExtra("email");

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnReset.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getResetPassSuccess().observe(this, success -> {
            if (success != null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish(); // close this activity and go back to login
            }
        });
    }

    private void setupListeners() {
        binding.btnReset.setOnClickListener(v -> {
            String newPassword = binding.edtResetPass.getText().toString().trim();
            String confirmPassword = binding.edtResetConfirm.getText().toString().trim();
            viewModel.resetPassword(email, newPassword, confirmPassword);
        });
    }
}