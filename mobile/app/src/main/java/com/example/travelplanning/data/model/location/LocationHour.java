package com.example.travelplanning.data.model.location;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationHour implements Parcelable {
    String id;
    int dayOfWeek; // 0: Sunday, 1: Monday...
    int openTime;  // Phút (vd: 510 = 08:30)
    int closeTime; // Phút (vd: 1020 = 17:00)

    protected LocationHour(Parcel in) {
        id = in.readString();
        dayOfWeek = in.readInt();
        openTime = in.readInt();
        closeTime = in.readInt();
    }

    public String getFormattedOpen() { return formatTime(openTime); }
    public String getFormattedClose() { return formatTime(closeTime); }

    private String formatTime(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    public static final Creator<LocationHour> CREATOR = new Creator<LocationHour>() {
        @Override
        public LocationHour createFromParcel(Parcel in) {
            return new LocationHour(in);
        }

        @Override
        public LocationHour[] newArray(int size) {
            return new LocationHour[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(dayOfWeek);
        dest.writeInt(openTime);
        dest.writeInt(closeTime);
    }
}