/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.bilibili.boxing.model.config;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * The cropping config, a cropped photo uri is needed at least.
 *
 * @author ChenSL
 */
public class BoxingCropOption implements Parcelable {
    private Uri mDestination;
    private float mAspectRatioX;
    private float mAspectRatioY;
    private int mMaxWidth;
    private int mMaxHeight;

    public BoxingCropOption(Uri destination) {
        this.mDestination = destination;
    }

    public static BoxingCropOption with(@NonNull Uri destination) {
        return new BoxingCropOption(destination);
    }

    public BoxingCropOption aspectRatio(float x, float y) {
        this.mAspectRatioX = x;
        this.mAspectRatioY = y;
        return this;
    }

    public BoxingCropOption useSourceImageAspectRatio() {
        this.mAspectRatioX = 0;
        this.mAspectRatioY = 0;
        return this;
    }

    public BoxingCropOption withMaxResultSize(int width, int height) {
        this.mMaxWidth = width;
        this.mMaxHeight = height;
        return this;
    }


    public float getAspectRatioX() {
        return mAspectRatioX;
    }

    public float getAspectRatioY() {
        return mAspectRatioY;
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    public Uri getDestination() {
        return mDestination;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mDestination, flags);
        dest.writeFloat(this.mAspectRatioX);
        dest.writeFloat(this.mAspectRatioY);
        dest.writeInt(this.mMaxWidth);
        dest.writeInt(this.mMaxHeight);
    }

    BoxingCropOption(Parcel in) {
        this.mDestination = in.readParcelable(Uri.class.getClassLoader());
        this.mAspectRatioX = in.readFloat();
        this.mAspectRatioY = in.readFloat();
        this.mMaxWidth = in.readInt();
        this.mMaxHeight = in.readInt();
    }

    public static final Creator<BoxingCropOption> CREATOR = new Creator<BoxingCropOption>() {
        @Override
        public BoxingCropOption createFromParcel(Parcel source) {
            return new BoxingCropOption(source);
        }

        @Override
        public BoxingCropOption[] newArray(int size) {
            return new BoxingCropOption[size];
        }
    };
}