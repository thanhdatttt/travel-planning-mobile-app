package com.example.travelplanning.ui.mainscreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.travelplanning.databinding.FragmentHomeBinding;
import com.example.travelplanning.ui.location.LocationSearchActivity; // Đảm bảo import đúng package của bạn

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bắt sự kiện click vào thanh tìm kiếm giả
        binding.tvDummySearch.setOnClickListener(v -> {
            // Chuyển sang màn hình tìm kiếm thực sự
            Intent intent = new Intent(requireActivity(), LocationSearchActivity.class);
            startActivity(intent);
            
            // Thêm hiệu ứng chuyển màn hình mượt mà (Fade in)
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}