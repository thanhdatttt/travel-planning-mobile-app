package com.example.travelplanning.viewmodel.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.travelplanning.R;
import com.example.travelplanning.ui.account.AccountOption;

import java.util.ArrayList;
import java.util.List;

public class AccountViewModel extends ViewModel {
    public static final int ID_INFO = 1;
    public static final int ID_PASSWORD = 2;
    public static final int ID_LOGOUT = 3;
    private final MutableLiveData<List<AccountOption>> _menuItems = new MutableLiveData<>();

    public LiveData<List<AccountOption>> getMenuItems() {
        return _menuItems;
    }

    public AccountViewModel() {
        loadMenuData();
    }

    private void loadMenuData() {
        List<AccountOption> list = new ArrayList<>();
        list.add(new AccountOption(ID_INFO, R.drawable.ic_user, "Thông tin cá nhân"));
        list.add(new AccountOption(ID_PASSWORD, R.drawable.ic_setting, "Cài đặt"));
        list.add(new AccountOption(ID_LOGOUT, R.drawable.ic_logout, "Đăng xuất"));

        _menuItems.setValue(list);
    }

}