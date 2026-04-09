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
import androidx.navigation.NavController;
import com.example.travelplanning.R;
import com.example.travelplanning.databinding.ActivityLocationSearchBinding;
import com.example.travelplanning.ui.location.LocationAdapter;
import com.example.travelplanning.ui.location_detail.LocationDetailFragment;
import com.example.travelplanning.viewmodel.location.LocationViewModel;
import com.example.travelplanning.viewmodel.category.CategoryViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.content.Intent;
import com.example.travelplanning.ui.location_detail.LocationDetailFragment;
public class LocationSearchActivity extends AppCompatActivity {
    private LocationViewModel locationViewModel;
    private LocationAdapter adapter;
    
    private ImageButton btnBack; 
    private EditText edtSearch;
    private ImageButton btnFilter;
    private RecyclerView rvLocations;
    private ProgressBar progressBar;

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
        
        currentPage = 1;
        performSearch();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack); 
        edtSearch = findViewById(R.id.edtSearch);
        btnFilter = findViewById(R.id.btnFilter);
        rvLocations = findViewById(R.id.rvLocations);
        progressBar = findViewById(R.id.progressBar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvLocations.setLayoutManager(layoutManager);

        rvLocations.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new LocationAdapter();
        rvLocations.setAdapter(adapter);
    }

    private void setupViewModel() {
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        categoryViewModel.fetchAllCategories();
        
        locationViewModel.getSearchResults().observe(this, locations -> {
            if (locations != null && !locations.isEmpty()) {
                adapter.setList(locations);
                
                adapter.setOnLocationClickListener(location -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("location_id", location.getId());
                    
                    Toast.makeText(this, "Đang mở: " + location.getName(), Toast.LENGTH_SHORT).show();
                    
                });
            }
        });

        locationViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish()); 

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
               (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                
                currentQuery = edtSearch.getText().toString().trim();
                currentPage = 1; 
                performSearch();
                
                hideKeyboard();
                return true;
            }
            return false;
        });

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

                boolean isLoding = locationViewModel.getIsLoading().getValue() != null && locationViewModel.getIsLoading().getValue();
                boolean hasMore = locationViewModel.getHasMoreData().getValue() != null && locationViewModel.getHasMoreData().getValue();

                if (!isLoding && hasMore) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        
                        currentPage++; 
                        performSearch(); 
                    }
                }
            }
        });
    }

    private void performSearch() {
        locationViewModel.searchLocations(currentQuery, currentCategoryId, currentPriceLevel, currentPage, 15);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
    }

 
    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.dialog_location_filter, null);
        bottomSheetDialog.setContentView(view);

        ChipGroup chipGroupPrice = view.findViewById(R.id.chipGroupPrice);
        ChipGroup chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        MaterialButton btnResetFilter = view.findViewById(R.id.btnResetFilter);
        MaterialButton btnApplyFilter = view.findViewById(R.id.btnApplyFilter);

        categoryViewModel.getCategories().observe(this, categories -> {
            if (categories == null) return;
            
            chipGroupCategory.removeAllViews();
            for (com.example.travelplanning.data.model.category.Category cat : categories) {
                Chip chip = new Chip(this);
                chip.setText(cat.getNameVi()); 
                chip.setCheckable(true);
                chip.setTag(cat.getId()); 
                
                if (currentCategoryId != null && currentCategoryId.equals(cat.getId())) {
                    chip.setChecked(true);
                }
                
                chipGroupCategory.addView(chip);
            }
        });

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

        btnResetFilter.setOnClickListener(v -> {
            chipGroupPrice.clearCheck();
            chipGroupCategory.clearCheck(); 
            currentPriceLevel = null;
            currentCategoryId = null;      
            
            currentPage = 1;
            performSearch();
            bottomSheetDialog.dismiss();
        });

        btnApplyFilter.setOnClickListener(v -> {
            int selectedPriceId = chipGroupPrice.getCheckedChipId();
            if (selectedPriceId != View.NO_ID) {
                Chip selectedChip = view.findViewById(selectedPriceId);
                Object tagValue = selectedChip.getTag();
                if (tagValue != null) {

                    currentPriceLevel = Integer.valueOf(tagValue.toString());
                }
            } else {
                currentPriceLevel = null; 
            }

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