package com.example.travelplanning.data.model.location;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo implements Parcelable {
    String id;
    String url;
    Boolean isFeature;

    protected Photo(Parcel in) {
        id = in.readString();
        url = in.readString();
        byte tmpIsFeature = in.readByte();
        isFeature = tmpIsFeature == 0 ? null : tmpIsFeature == 1;
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeByte((byte) (isFeature == null ? 0 : isFeature ? 1 : 2));
    }
}