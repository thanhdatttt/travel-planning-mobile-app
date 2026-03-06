package com.example.travelplanning.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;
import com.example.travelplanning.models.AccountMenuItem;

import java.util.ArrayList;
import java.util.List;

import com.example.travelplanning.adapters.AccountMenuAdapter;

public class AccountMenuActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_menu);

        RecyclerView rvAccountMenus = findViewById(R.id.rvAccountMenu);

        List<AccountMenuItem> data = new ArrayList<>();
        data.add(new AccountMenuItem("Thông tin cá nhân", R.drawable.ic_user));
        data.add(new AccountMenuItem("Đánh giá của tôi", R.drawable.ic_star));
        data.add(new AccountMenuItem("Địa điểm yêu thích", R.drawable.ic_heart));
        data.add(new AccountMenuItem("Cài đặt", R.drawable.ic_setting));
        data.add(new AccountMenuItem("Đăng xuất", R.drawable.ic_logout));

        // Thiết lập RecyclerView
        AccountMenuAdapter adapter = new AccountMenuAdapter(data);
        rvAccountMenus.setLayoutManager(new LinearLayoutManager(this));
        rvAccountMenus.setAdapter(adapter);
    }
}
