package com.example.travelplanning.data.model.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location implements Parcelable{
    String id;
    String name;
    String address;
    Double avgRating;
    String description;
    String phone;
    String website;
    Integer priceLevel;
    String description;
    Double latitude;
    Double longitude;
    Double distance;

    String categoryName;
    String categoryIcon;
    String imageUrl;
    @Builder.Default
    List<Photo> photos = new ArrayList<>();
    Integer ratingCount;

    protected Location(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        description = in.readString();
        phone = in.readString();
        website = in.readString();
        if (in.readByte() == 0) avgRating = null; else avgRating = in.readDouble();
        if (in.readByte() == 0) priceLevel = null; else priceLevel = in.readInt();
        if (in.readByte() == 0) latitude = null; else latitude = in.readDouble();
        if (in.readByte() == 0) longitude = null; else longitude = in.readDouble();
        if (in.readByte() == 0) distance = null; else distance = in.readDouble();
        categoryName = in.readString();
        categoryIcon = in.readString();
        imageUrl = in.readString();
        photos = in.createTypedArrayList(Photo.CREATOR);
        if (in.readByte() == 0) ratingCount = null; else ratingCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(description);
        dest.writeString(phone);
        dest.writeString(website);
        if (avgRating == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(avgRating); }
        if (priceLevel == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeInt(priceLevel); }
        if (latitude == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(latitude); }
        if (longitude == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(longitude); }
        if (distance == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(distance); }
        dest.writeString(categoryName);
        dest.writeString(categoryIcon);
        dest.writeString(imageUrl);
        dest.writeTypedList(photos);
        if (ratingCount == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeInt(ratingCount); }
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) { return new Location(in); }
        @Override
        public Location[] newArray(int size) { return new Location[size]; }
    };
}