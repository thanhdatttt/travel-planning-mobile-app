package com.example.travelplanning.ui.location;
import androidx.recyclerview.widget.DividerItemDecoration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelplanning.R;
import com.example.travelplanning.ui.adapter.LocationAdapter;
import com.example.travelplanning.viewmodel.location.LocationViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class LocationSearchActivity extends AppCompatActivity {

    private LocationViewModel locationViewModel;
    private LocationAdapter adapter;
    
    private EditText edtSearch;
    private ImageButton btnFilter;
    private RecyclerView rvLocations;
    private ProgressBar progressBar;

    // Các biến lưu trạng thái Filter hiện tại
    private Integer currentCategoryId = null;
    private Integer currentPriceLevel = null;
    private String currentQuery = "";
    private int currentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        initViews();
        setupViewModel();
        setupListeners();
        
        // Tự động load danh sách ban đầu (không từ khóa)
        performSearch();
    }

    private void initViews() {
        edtSearch = findViewById(R.id.edtSearch);
        btnFilter = findViewById(R.id.btnFilter);
        rvLocations = findViewById(R.id.rvLocations);
        progressBar = findViewById(R.id.progressBar);
        
        // THIẾT LẬP LAYOUT MANAGER
        rvLocations.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // THÊM ĐƯỜNG KẺ PHÂN CÁCH (Divider)
        rvLocations.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new LocationAdapter();
        rvLocations.setAdapter(adapter);
    }

    private void setupViewModel() {
        // Khởi tạo ViewModel
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);

        // Quan sát danh sách kết quả
        locationViewModel.getSearchResults().observe(this, locations -> {
            if (locations != null) {
                adapter.setList(locations);
            }
        });

        // Quan sát trạng thái Loading
        locationViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Quan sát lỗi
        locationViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // Bắt sự kiện người dùng nhấn nút "Kính lúp/Enter" trên bàn phím ảo
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
               (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                
                currentQuery = edtSearch.getText().toString().trim();
                currentPage = 1; // Reset về trang 1 khi search mới
                performSearch();
                
                // TODO: Ẩn bàn phím ảo
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Mở Bottom Sheet Filter
        btnFilter.setOnClickListener(v -> showFilterBottomSheet());
    }

    private void performSearch() {
        // Gọi API thông qua ViewModel đã viết ở bước trước
        locationViewModel.searchLocations(currentQuery, currentCategoryId, currentPriceLevel, currentPage, 15);
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        // Inflate layout dialog_location_filter.xml (Bạn tự tạo layout này dựa trên ảnh Filter.png nhé)
        View view = getLayoutInflater().inflate(R.layout.dialog_location_filter, null);
        bottomSheetDialog.setContentView(view);

        // TODO: Xử lý logic chọn Category, Price trong Dialog ở đây.
        // Khi người dùng bấm nút "Tìm kiếm" màu xanh lá trong Dialog:
        /*
        Button btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnApplyFilter.setOnClickListener(v -> {
            // Lấy dữ liệu từ các input trong dialog
            // currentCategoryId = ...
            // currentPriceLevel = ...
            currentPage = 1;
            performSearch();
            bottomSheetDialog.dismiss();
        });
        */

        bottomSheetDialog.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}