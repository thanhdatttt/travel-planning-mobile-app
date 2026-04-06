package com.example.travelplanning.ui.location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;
import com.example.travelplanning.databinding.ActivityLocationSearchBinding;
import com.example.travelplanning.ui.adapter.LocationAdapter;
import com.example.travelplanning.ui.location_detail.LocationDetailFragment;
import com.example.travelplanning.viewmodel.location.LocationViewModel;
import com.example.travelplanning.viewmodel.category.CategoryViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class LocationSearchActivity extends AppCompatActivity {
    private LocationViewModel locationViewModel;
    private LocationAdapter adapter;
    
    // UI
    private ImageButton btnBack; // THÊM NÚT BACK
    private EditText edtSearch;
    private ImageButton btnFilter;
    private RecyclerView rvLocations;
    private ProgressBar progressBar;

    // Các biến lưu trạng thái Filter hiện tại
    private Integer currentCategoryId = null;
    private Integer currentPriceLevel = null;
    private String currentQuery = "";
    private int currentPage = 1;
    private CategoryViewModel categoryViewModel;
    private ActivityLocationSearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        initViews();
        setupViewModel();
        setupListeners();
        
        // Tự động load danh sách ban đầu (không từ khóa)
        currentPage = 1;
        performSearch();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack); // Ánh xạ nút Back
        edtSearch = findViewById(R.id.edtSearch);
        btnFilter = findViewById(R.id.btnFilter);
        rvLocations = findViewById(R.id.rvLocations);
        progressBar = findViewById(R.id.progressBar);

        // 1. THIẾT LẬP LAYOUT MANAGER CỐ ĐỊNH
        rvLocations.setLayoutManager(new LinearLayoutManager(this));

        // 2. THÊM ĐƯỜNG KẺ PHÂN CÁCH (Divider)
        rvLocations.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Khởi tạo Adapter rỗng trước
        adapter = new LocationAdapter();
        rvLocations.setAdapter(adapter);
    }

    private void setupViewModel() {
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.fetchAllCategories();
        locationViewModel.getSearchResults().observe(this, locations -> {
            if (locations != null) {
                // Giả sử Adapter của bạn đã có hàm setList(locations)
                adapter.setList(locations);
                adapter.setOnLocationClickListener(location -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("location_id", location.getId());

                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.nav_location_detail, bundle);
                });
            }
        });

        locationViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        locationViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        // NÚT BACK (Quay lại trang chủ/trang trước)
        btnBack.setOnClickListener(v -> finish()); // Đóng activity này

        // Bàn phím ảo -> nhấn Enter/Search
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
               (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                
                currentQuery = edtSearch.getText().toString().trim();
                currentPage = 1; 
                performSearch();
                
                // ẨN BÀN PHÍM ảo
                hideKeyboard();
                return true;
            }
            return false;
        });

        // Mở Bottom Sheet Filter
        btnFilter.setOnClickListener(v -> showFilterBottomSheet());
        rvLocations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // Kiểm tra điều kiện load more: 
                // 1. Không đang loading
                // 2. Còn dữ liệu để load (hasMoreData)
                // 3. Đã cuộn đến cuối (item cuối cùng đang hiển thị)
                boolean isLoding = locationViewModel.getIsLoading().getValue() != null && locationViewModel.getIsLoading().getValue();
                boolean hasMore = locationViewModel.getHasMoreData().getValue() != null && locationViewModel.getHasMoreData().getValue();

                if (!isLoding && hasMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        
                        currentPage++; // Tăng biến trang toàn cục của Activity
                        performSearch(); // Gọi lại hàm search với currentPage mới
                    }
                }
            }
        });
    }

    private void performSearch() {
        // Gọi API với đầy đủ tham số
        locationViewModel.searchLocations(currentQuery, currentCategoryId, currentPriceLevel, currentPage, 15);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
    }

    //---------------------------------------------------------
    // XỬ LÝ LOGIC FILTER TỪ BẢNG BOTTOM SHEET
    //---------------------------------------------------------
    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_location_filter, null);
        bottomSheetDialog.setContentView(view);

        // 1. Ánh xạ các View
        ChipGroup chipGroupPrice = view.findViewById(R.id.chipGroupPrice);
        ChipGroup chipGroupCategory = view.findViewById(R.id.chipGroupCategory); // THÊM DÒNG NÀY
        MaterialButton btnResetFilter = view.findViewById(R.id.btnResetFilter);
        MaterialButton btnApplyFilter = view.findViewById(R.id.btnApplyFilter);

        // 2. ĐỔ DỮ LIỆU CATEGORY VÀO CHIPGROUP (Phần mới quan trọng)
        categoryViewModel.getCategories().observe(this, categories -> {
            if (categories == null) return;
            
            chipGroupCategory.removeAllViews();
            for (com.example.travelplanning.data.model.category.Category cat : categories) {
                Chip chip = new Chip(this);
                chip.setText(cat.getNameVi()); // Hiển thị tên tiếng Việt
                chip.setCheckable(true);
                chip.setTag(cat.getId()); // Lưu ID vào Tag
                
                // Đồng bộ trạng thái đã chọn trước đó
                if (currentCategoryId != null && currentCategoryId.equals(cat.getId())) {
                    chip.setChecked(true);
                }
                
                chipGroupCategory.addView(chip);
            }
        });

        // 3. ĐỒNG BỘ MỨC GIÁ (Giữ nguyên code cũ của bạn)
        if (currentPriceLevel != null) {
            int childCount = chipGroupPrice.getChildCount();
            for (int i = 0; i < childCount; i++) {
                Chip chip = (Chip) chipGroupPrice.getChildAt(i);
                if (chip.getTag() != null && Integer.parseInt(chip.getTag().toString()) == currentPriceLevel) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        // 4. NÚT ĐẶT LẠI (RESET) - Bổ sung reset Category
        btnResetFilter.setOnClickListener(v -> {
            chipGroupPrice.clearCheck();
            chipGroupCategory.clearCheck(); // THÊM DÒNG NÀY
            currentPriceLevel = null;
            currentCategoryId = null;       // THÊM DÒNG NÀY
            
            currentPage = 1;
            performSearch();
            bottomSheetDialog.dismiss();
        });

        // 5. NÚT ÁP DỤNG (APPLY) - Bổ sung lấy ID Category
        btnApplyFilter.setOnClickListener(v -> {
            int selectedPriceId = chipGroupPrice.getCheckedChipId();
            if (selectedPriceId != View.NO_ID) {
                Chip selectedChip = view.findViewById(selectedPriceId);
                Object tagValue = selectedChip.getTag();
                if (tagValue != null) {
                    // Ép kiểu an toàn
                    currentPriceLevel = Integer.valueOf(tagValue.toString());
                }
            } else {
                currentPriceLevel = null; // Bỏ chọn lọc theo giá
            }

            // Lấy CategoryId (PHẦN MỚI)
            int selectedCatId = chipGroupCategory.getCheckedChipId();
            if (selectedCatId != View.NO_ID) {
                Chip selectedChip = chipGroupCategory.findViewById(selectedCatId);
                currentCategoryId = (Integer) selectedChip.getTag();
            } else {
                currentCategoryId = null;
            }

            currentPage = 1;
            performSearch();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}