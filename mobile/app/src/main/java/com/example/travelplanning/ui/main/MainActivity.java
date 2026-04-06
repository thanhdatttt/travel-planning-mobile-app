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
import com.example.travelplanning.ui.auth.AuthActivity;
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

        TokenManager.saveRefreshToken(this,"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIwYmIzMzQ1Yy0yYTkxLTQ4NjYtYjU5OC03NWFhZDQ2YTBmZjciLCJpYXQiOjE3NzQ3MDkwMjUsImV4cCI6MjM3OTUwOTAyNX0.pgWyU_Ku9ugrM91puOwCTjZLM-k5-7Bme8ejz0etpk0");

        // init splash screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        //hard code token for testing
        String manualAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjODIyOGZkYi0wNDNmLTQyY2ItOGE0Yy1mZDE1MDQ1ZDUxNjAiLCJpYXQiOjE3NzU0NjQ0OTgsImV4cCI6MTc3NTU1MDg5OH0.xZIhsMkPT96DUgF_n1uXwoQh2cHOCnh4e8-mu7zoUjU";
        String manualRefreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIyNzAxYTk2Ny0zMTYyLTQ4ZjctOWIyYS1kODc5MmRmYzY2OTciLCJpYXQiOjE3NzQxMzg0NzUsImV4cCI6MjM3ODkzODQ3NX0.PDEAHbhlHWr8v6ZsLR_p1BMV6JOxjc-nXpPdJjisxuc";
        TokenManager.saveTokens(this, manualAccessToken, manualRefreshToken);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Keep the splash screen displayed until ready
        splashScreen.setKeepOnScreenCondition(() -> !isReady);

        EdgeToEdge.enable(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isReady = true; //let splash screen dismiss
            startNavigation();
        }, 2000);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // navigate after splash screen
    private void startNavigation() {
        String token = TokenManager.getAccessToken(this);

        Intent intent;
        if (token != null && !token.isEmpty()) {
            // has token , go to main screen
            intent = new Intent(MainActivity.this, MainScreenActivity.class);
        } else {
            // no token, go to login
            intent = new Intent(MainActivity.this, AuthActivity.class);
        }
        startActivity(intent);
        finish();
    }

}