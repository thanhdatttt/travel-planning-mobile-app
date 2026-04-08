package com.example.travelplanning.ui.location;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.location.Location;
import java.util.ArrayList;
import java.util.List;
public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<Location> locationList = new ArrayList<>();

    public void setList(List<Location> newList) {
        this.locationList = new ArrayList<>(newList); 
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location location = locationList.get(position);
        
        // Set tên địa điểm
        holder.tvName.setText(location.getName());
        
        // Lấy và kiểm tra dữ liệu rating/ratingCount an toàn
        double avgRating = location.getAvgRating() != null ? location.getAvgRating() : 0.0;
        int ratingCount = location.getRatingCount() != null ? location.getRatingCount() : 0;

        // Định dạng chuỗi hiển thị số rating trước ngôi sao: "4.5/5 (120)"
        // Ngôi sao đã có ở drawableEnd (bên phải) trong XML
        String fullRatingText = String.format(java.util.Locale.US, "%.1f/5 (%d)", avgRating, ratingCount);
        holder.tvRating.setText(fullRatingText);

        // Load ảnh bằng Glide (centerCrop cơ bản vì MaterialCardView sẽ bo góc)
        if (location.getImageUrl() != null && !location.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(location.getImageUrl())
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLocationClick(location);
        });
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvRating;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivLocationImage);
            tvName = itemView.findViewById(R.id.tvLocationName);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }

    public interface OnLocationClickListener {
        void onLocationClick(Location location);
    }

    private OnLocationClickListener listener;

    public void setOnLocationClickListener(OnLocationClickListener listener) {
        this.listener = listener;
    }

}