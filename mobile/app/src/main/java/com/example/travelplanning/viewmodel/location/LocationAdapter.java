package com.example.travelplanning.ui.map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.location.Location;
import org.osmdroid.util.GeoPoint;
import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private List<Location> locationList = new ArrayList<>();
    private GeoPoint currentUserLocation;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Location location);
    }

    public LocationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<Location> locations, GeoPoint userLocation) {
        this.locationList = locations != null ? locations : new ArrayList<>();
        this.currentUserLocation = userLocation;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new LocationViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        Location loc = locationList.get(position);
        
        holder.tvName.setText(loc.getName());
        
        String priceStr = "";
        if (loc.getPriceLevel() != null && loc.getPriceLevel() > 0) {
            StringBuilder sb = new StringBuilder(" • ");
            for (int i = 0; i < loc.getPriceLevel(); i++) sb.append("$");
            priceStr = sb.toString();
        }

        double rating = loc.getAvgRating() != null ? loc.getAvgRating() : 0.0;
        int count = loc.getRatingCount() != null ? loc.getRatingCount() : 0;
        holder.tvRating.setText("⭐ " + rating + " (" + count + ")" + priceStr);

        String distanceStr = "";
        if (currentUserLocation != null && loc.getLatitude() != null && loc.getLongitude() != null) {
            GeoPoint placeGeo = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            double meters = currentUserLocation.distanceToAsDouble(placeGeo);
            distanceStr = (meters > 1000) ? String.format("%.1f km", meters / 1000) : Math.round(meters) + " m";
            distanceStr += " • ";
        }
        
        String address = loc.getAddress() != null ? loc.getAddress() : "Chưa có địa chỉ";
        holder.tvDistanceAddress.setText(distanceStr + address);

        Glide.with(holder.itemView.getContext())
            .load(loc.getImageUrl())
            .transform(new CenterCrop(), new RoundedCorners(16))
            .placeholder(R.drawable.ic_placeholder) 
            .into(holder.imgPlace);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(loc));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPlace;
        TextView tvName, tvRating, tvDistanceAddress;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPlace = itemView.findViewById(R.id.imgPlace);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDistanceAddress = itemView.findViewById(R.id.tvDistanceAddress);
        }
    }
}