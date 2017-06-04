package com.bilibili.boxing.model.config;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

/**
 * Created by xiyangyuge on 2017/6/2.
 */

public class MediaFilter implements Parcelable {

    private String selection;

    public MediaFilter(int mediaMinWidth, int mediaMinHeight) {
        this(mediaMinWidth, mediaMinHeight, 0);
    }

    public MediaFilter(int mediaMinSize) {
        this(0, 0, mediaMinSize);
    }

    public MediaFilter(int mediaMinWidth, int mediaMinHeight, int mediaMinSize) {
        StringBuilder selectionBuilder = new StringBuilder();
        if (mediaMinWidth > 0) {
            selectionBuilder.append(MediaStore.Images.Media.WIDTH + " >= " + mediaMinWidth);
        }
        if (mediaMinHeight > 0) {
            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" and ");
            }
            selectionBuilder.append(MediaStore.Images.Media.HEIGHT + " >= " + mediaMinHeight);
        }
        if (mediaMinSize > 0) {
            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" and ");
            }
            selectionBuilder.append(MediaStore.Images.Media.SIZE + " >= " + mediaMinSize);
        }
        this.selection = selectionBuilder.length() > 0 ? selectionBuilder.toString() : null;
    }

    public String getSelection() {
        return selection;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.selection);
    }

    protected MediaFilter(Parcel in) {
        this.selection = in.readString();
    }

    public static final Creator<MediaFilter> CREATOR = new Creator<MediaFilter>() {
        @Override
        public MediaFilter createFromParcel(Parcel source) {
            return new MediaFilter(source);
        }

        @Override
        public MediaFilter[] newArray(int size) {
            return new MediaFilter[size];
        }
    };
}
