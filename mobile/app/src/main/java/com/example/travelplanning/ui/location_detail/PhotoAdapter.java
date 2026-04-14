package com.example.travelplanning.ui.location_detail;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.databinding.ItemLocationPhotoBinding;
import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    private List<Photo> photoList = new ArrayList<>();

    public void setList(List<Photo> newList) {
        this.photoList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLocationPhotoBinding binding = ItemLocationPhotoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PhotoViewHolder(binding);
    }

    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photoList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(photo.getUrl())
                .centerCrop()
                .placeholder(R.drawable.suprised_car)
                .into(holder.binding.ivLocationPhoto);
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ItemLocationPhotoBinding binding;
        PhotoViewHolder(ItemLocationPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}