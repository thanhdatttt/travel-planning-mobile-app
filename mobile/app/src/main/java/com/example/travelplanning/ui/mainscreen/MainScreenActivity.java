package com.example.travelplanning.ui.mainscreen;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.travelplanning.R;
import com.example.travelplanning.databinding.ActivityMainScreenBinding;

public class MainScreenActivity extends AppCompatActivity {
    private ActivityMainScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = ActivityMainScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);


//        TEMPORARY!!!
        binding.btnAdmin.setOnClickListener(v -> {
            // R.id.adminFragment là ID bạn đã đặt trong nav_graph.xml
            navController.navigate(R.id.nav_admin);
        });
    }
}
