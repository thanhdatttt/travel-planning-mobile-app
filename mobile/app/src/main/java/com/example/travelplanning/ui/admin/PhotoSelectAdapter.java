package com.example.travelplanning.ui.admin;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelplanning.data.model.location.Photo;

import java.util.List;

public class PhotoSelectAdapter extends RecyclerView.Adapter<PhotoSelectAdapter.PhotoVH> {
    private final List<Photo> photos;
    private final OnPhotoSelectedListener listener;

    public interface OnPhotoSelectedListener { void onSelected(String url); }

    public PhotoSelectAdapter(List<com.example.travelplanning.data.model.location.Photo> photos, OnPhotoSelectedListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoVH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        ImageView iv = new ImageView(p.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(250, 250)); // Kích thước ảnh nhỏ
        iv.setPadding(8, 8, 8, 8);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new PhotoVH(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoVH h, int p) {
        String url = photos.get(p).getUrl();
        Glide.with(h.itemView).load(url).into((ImageView) h.itemView);
        h.itemView.setOnClickListener(v -> listener.onSelected(url));
    }

    @Override public int getItemCount() { return photos != null ? photos.size() : 0; }
    static class PhotoVH extends RecyclerView.ViewHolder { public PhotoVH(@NonNull View v) { super(v); } }
}