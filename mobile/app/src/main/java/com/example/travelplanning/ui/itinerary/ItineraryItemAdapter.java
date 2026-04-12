package com.example.travelplanning.ui.itinerary;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.example.travelplanning.data.model.itinerary.ItineraryItem;

import java.util.Objects;

public class ItineraryItemAdapter {
//    private final OnItemClickListener onItemClick;
//
//    // handle click from fragment
//    public interface OnItemClickListener {
//        void onClick(ItineraryItem item);
//    }
//
//    public ItineraryItemAdapter(OnItemClickListener onItemClick) {
//        super(DIFF_CALLBACK);
//        this.onItemClick = onItemClick;
//    }
//
//    private static final DiffUtil.ItemCallback<ItineraryItem> DIFF_CALLBACK =
//            new DiffUtil.ItemCallback<ItineraryItem>() {
//                @Override
//                public boolean areItemsTheSame(ItineraryItem oldItem, ItineraryItem newItem) {
//                    return oldItem.getId().equals(newItem.getId());
//                }
//
//                @Override
//                public boolean areContentsTheSame(ItineraryItem oldItem, ItineraryItem newItem) {
//                    return Objects.equals(oldItem.getDate(), newItem.getDate())
//                            && Objects.equals(oldItem.getNote(), newItem.getNote())
//                            && Objects.equals(oldItem.getOrderIdx(), newItem.getOrderIdx())
//                            && Objects.equals(oldItem.getLocation(), newItem.getLocation());
//                }
//            };
//
//    @NonNull
//    @Override
//    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//    }
}
