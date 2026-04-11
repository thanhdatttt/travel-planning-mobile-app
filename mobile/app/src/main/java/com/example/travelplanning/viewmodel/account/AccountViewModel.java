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
    public static final int ID_SETTING = 2;
    public static final int ID_REVIEW = 3;
    public static final int ID_FAV = 4;
    public static final int ID_ADMIN = 5;
    public static final int ID_LOGOUT = 6;
    private final MutableLiveData<List<AccountOption>> _menuItems = new MutableLiveData<>();

    public LiveData<List<AccountOption>> getMenuItems() {
        return _menuItems;
    }

    public AccountViewModel() {
        loadMenuData();
    }

    private void loadMenuData() {
        List<AccountOption> list = new ArrayList<>();
//        list.add(new AccountOption(ID_INFO, R.drawable.ic_user, "Thông tin cá nhân"));
//        list.add(new AccountOption(ID_SETTING, R.drawable.ic_setting, "Cài đặt"));
//        list.add(new AccountOption(ID_LOGOUT, R.drawable.ic_logout, "Đăng xuất"));

        _menuItems.setValue(list);
    }

}