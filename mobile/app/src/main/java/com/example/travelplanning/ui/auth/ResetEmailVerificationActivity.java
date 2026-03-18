package com.example.travelplanning.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.databinding.ActivityResetEmailVerificationBinding;
import com.example.travelplanning.viewmodel.auth.RegisterViewModel;

public class ResetEmailVerificationActivity extends AppCompatActivity {
    private ActivityResetEmailVerificationBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetEmailVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnEmailResetContinue.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getOtpSent().observe(this, sent -> {
            if (sent != null && sent) {
                // OTP sent successfully
                startActivity(new Intent(this, OTPVerificationActivity.class)
                        .putExtra("email", binding.edtResetEmail.getText().toString().trim())
                        .putExtra("type", "reset"));
                finish(); // close this activity
            }
        });
    }

    private void setupListeners() {
        binding.btnEmailResetContinue.setOnClickListener(v -> {
            String email = binding.edtResetEmail.getText().toString().trim();
            viewModel.sendOTP(email, "reset");
        });
    }
}