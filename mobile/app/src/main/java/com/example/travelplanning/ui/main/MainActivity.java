package com.example.travelplanning.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelplanning.R;
import com.example.travelplanning.core.storage.TokenManager;
import com.example.travelplanning.databinding.ActivityMainBinding;
import com.example.travelplanning.ui.mainscreen.MainScreenActivity;
import com.example.travelplanning.ui.splash.SplashActivity;

import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private boolean isReady = false;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        //hard code token for testing
        String manualAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIyNzAxYTk2Ny0zMTYyLTQ4ZjctOWIyYS1kODc5MmRmYzY2OTciLCJpYXQiOjE3NzQxMzg0NzUsImV4cCI6MTc3NDIyNDg3NX0.NT0FjG9yu9Tg37_ty2Tj9Na0lQovIiy8zw3QoeDZV60";
        String manualRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIyNzAxYTk2Ny0zMTYyLTQ4ZjctOWIyYS1kODc5MmRmYzY2OTciLCJpYXQiOjE3NzQxMzg0NzUsImV4cCI6MjM3ODkzODQ3NX0.PDEAHbhlHWr8v6ZsLR_p1BMV6JOxjc-nXpPdJjisxuc";
        TokenManager.saveTokens(this, manualAccessToken, manualRefreshToken);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        splashScreen.setKeepOnScreenCondition(() -> !isReady);

        EdgeToEdge.enable(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isReady = true; //let splash screen dismiss
            navigateToMainScreen();
        }, 2000);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void navigateToMainScreen() {
        Intent intent = new Intent(MainActivity.this, MainScreenActivity.class);
        startActivity(intent);
        finish();
    }

}