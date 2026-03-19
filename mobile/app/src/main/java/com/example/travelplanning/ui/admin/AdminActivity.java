package com.example.travelplanning.ui.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travelplanning.R;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This links the Java file to the XML layout
        setContentView(R.layout.activity_admin);
    }
}