package com.example.travelplanning.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.ActivityLoginBinding;
import com.example.travelplanning.ui.home.MainActivity;
import com.example.travelplanning.viewmodel.auth.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {

        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!loading);
        });


        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });


        viewModel.getLoginSuccess().observe(this, res -> {
            if (res != null) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtEmail.getText().toString().trim();
            String pass = binding.edtPassword.getText().toString().trim();
            viewModel.login(email, pass);
        });

        binding.tvRegister.setOnClickListener(v -> {
            Toast.makeText(this, "Loading", Toast.LENGTH_SHORT).show();
        });

        binding.btnGoogle.setOnClickListener(v -> Toast.makeText(this, "Google Login...", Toast.LENGTH_SHORT).show());
        binding.btnFacebook.setOnClickListener(v -> Toast.makeText(this, "Facebook Login...", Toast.LENGTH_SHORT).show());
    }
}