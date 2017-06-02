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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

/**
 * The pick config.<br/>
 * 1.{@link Mode} is necessary. <br/>
 * 2.specify functions: camera, gif, paging. <br/>
 * calling {@link #needCamera(int)} to displayThumbnail a camera icon. <br/>
 * calling {@link #needGif()} to displayThumbnail gif photos. <br/>
 * calling {@link #needPaging(boolean)} to create load medias page by page, by default is true.
 *
 * @author ChenSL
 */
public class BoxingConfig implements Parcelable {
    public static final int DEFAULT_SELECTED_COUNT = 9;

    private Mode mMode = Mode.SINGLE_IMG;
    private ViewMode mViewMode = ViewMode.PREVIEW;
    private BoxingCropOption mCropOption;

    private int mMediaPlaceHolderRes;
    private int mMediaCheckedRes;
    private int mMediaUnCheckedRes;
    private int mAlbumPlaceHolderRes;
    private int mVideoDurationRes;
    private int mCameraRes;

    private boolean mNeedCamera;
    private boolean mNeedGif;
    private boolean mNeedPaging = true;

    private int mMaxCount = DEFAULT_SELECTED_COUNT;

    public enum Mode {
        SINGLE_IMG, MULTI_IMG, VIDEO
    }

    public enum ViewMode {
        PREVIEW, EDIT, PRE_EDIT
    }

    public BoxingConfig() {
    }

    public BoxingConfig(Mode mode) {
        this.mMode = mode;
    }

    public boolean isNeedCamera() {
        return mNeedCamera;
    }

    public boolean isNeedPaging() {
        return mNeedPaging;
    }

    public Mode getMode() {
        return mMode;
    }

    public ViewMode getViewMode() {
        return mViewMode;
    }

    public BoxingCropOption getCropOption() {
        return mCropOption;
    }

    /**
     * get the max count set by {@link #withMaxCount(int)}, otherwise return 9.
     */
    public int getMaxCount() {
        if (mMaxCount > 0) {
            return mMaxCount;
        }
        return DEFAULT_SELECTED_COUNT;
    }

    /**
     * get the image drawable resource by {@link BoxingConfig#withMediaPlaceHolderRes(int)}.
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    public @DrawableRes int getMediaPlaceHolderRes() {
        return mMediaPlaceHolderRes;
    }

    /**
     * get the media checked drawable resource by {@link BoxingConfig#withMediaCheckedRes(int)}.
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    public @DrawableRes  int getMediaCheckedRes() {
        return mMediaCheckedRes;
    }

    /**
     * get the media unchecked drawable resource by {@link BoxingConfig#withMediaUncheckedRes(int)} (int)}.
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    public @DrawableRes int getMediaUnCheckedRes() {
        return mMediaUnCheckedRes;
    }

    /**
     * get the media unchecked drawable resource by {@link BoxingConfig#withMediaPlaceHolderRes(int)}.
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    public @DrawableRes int getCameraRes() {
        return mCameraRes;
    }

    /**
     * get the album drawable resource by {@link BoxingConfig#withAlbumPlaceHolderRes(int)}.
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    public @DrawableRes int getAlbumPlaceHolderRes() {
        return mAlbumPlaceHolderRes;
    }

    /**
     * get the video drawable resource by {@link BoxingConfig#withVideoDurationRes(int)}.
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    public @DrawableRes int getVideoDurationRes() {
        return mVideoDurationRes;
    }

    public boolean isNeedLoading() {
        return mViewMode == ViewMode.EDIT;
    }

    public boolean isNeedEdit() {
        return mViewMode != ViewMode.PREVIEW;
    }

    public boolean isVideoMode() {
        return mMode == Mode.VIDEO;
    }

    public boolean isMultiImageMode() {
        return mMode == Mode.MULTI_IMG;
    }

    public boolean isSingleImageMode() {
        return mMode == Mode.SINGLE_IMG;
    }

    public boolean isNeedGif() {
        return mNeedGif;
    }

    /**
     * call this means gif is needed.
     */
    public BoxingConfig needGif() {
        this.mNeedGif = true;
        return this;
    }

    /**
     * set the camera res.
     */
    public BoxingConfig needCamera(@DrawableRes int cameraRes) {
        this.mCameraRes = cameraRes;
        this.mNeedCamera = true;
        return this;
    }

    /**
     * call this means paging is needed,by default is true.
     */
    public BoxingConfig needPaging(boolean needPaging) {
        this.mNeedPaging = needPaging;
        return this;
    }

    public BoxingConfig withViewer(ViewMode viewMode) {
        this.mViewMode = viewMode;
        return this;
    }

    public BoxingConfig withCropOption(BoxingCropOption cropOption) {
        this.mCropOption = cropOption;
        return this;
    }

    /**
     * set the max count of selected medias in {@link Mode#MULTI_IMG}
     * @param count max count
     */
    public BoxingConfig withMaxCount(int count) {
        if (count < 1) {
            return this;
        }
        this.mMaxCount = count;
        return this;
    }

    /**
     * set the image placeholder, default 0
     */
    public BoxingConfig withMediaPlaceHolderRes(@DrawableRes int mediaPlaceHolderRes) {
        this.mMediaPlaceHolderRes = mediaPlaceHolderRes;
        return this;
    }

    /**
     * set the image placeholder, otherwise use default drawable.
     */
    public BoxingConfig withMediaCheckedRes(@DrawableRes int mediaCheckedResRes) {
        this.mMediaCheckedRes = mediaCheckedResRes;
        return this;
    }

    /**
     * set the image placeholder, otherwise use default drawable.
     */
    public BoxingConfig withMediaUncheckedRes(@DrawableRes int mediaUncheckedRes) {
        this.mMediaUnCheckedRes = mediaUncheckedRes;
        return this;
    }

    /**
     * set the album placeholder, default 0
     */
    public BoxingConfig withAlbumPlaceHolderRes(@DrawableRes int albumPlaceHolderRes) {
        this.mAlbumPlaceHolderRes = albumPlaceHolderRes;
        return this;
    }

    /**
     * set the video duration resource in video mode, default 0
     */
    public BoxingConfig withVideoDurationRes(@DrawableRes int videoDurationRes) {
        this.mVideoDurationRes = videoDurationRes;
        return this;
    }

    @Override
    public String toString() {
        return "BoxingConfig{" +
                "mMode=" + mMode +
                ", mViewMode=" + mViewMode +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mMode == null ? -1 : this.mMode.ordinal());
        dest.writeInt(this.mViewMode == null ? -1 : this.mViewMode.ordinal());
        dest.writeParcelable(this.mCropOption, flags);
        dest.writeInt(this.mMediaPlaceHolderRes);
        dest.writeInt(this.mMediaCheckedRes);
        dest.writeInt(this.mMediaUnCheckedRes);
        dest.writeInt(this.mAlbumPlaceHolderRes);
        dest.writeInt(this.mVideoDurationRes);
        dest.writeInt(this.mCameraRes);
        dest.writeByte(this.mNeedCamera ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mNeedGif ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mNeedPaging ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mMaxCount);
    }

    protected BoxingConfig(Parcel in) {
        int tmpMMode = in.readInt();
        this.mMode = tmpMMode == -1 ? null : Mode.values()[tmpMMode];
        int tmpMViewMode = in.readInt();
        this.mViewMode = tmpMViewMode == -1 ? null : ViewMode.values()[tmpMViewMode];
        this.mCropOption = in.readParcelable(BoxingCropOption.class.getClassLoader());
        this.mMediaPlaceHolderRes = in.readInt();
        this.mMediaCheckedRes = in.readInt();
        this.mMediaUnCheckedRes = in.readInt();
        this.mAlbumPlaceHolderRes = in.readInt();
        this.mVideoDurationRes = in.readInt();
        this.mCameraRes = in.readInt();
        this.mNeedCamera = in.readByte() != 0;
        this.mNeedGif = in.readByte() != 0;
        this.mNeedPaging = in.readByte() != 0;
        this.mMaxCount = in.readInt();
    }

    public static final Creator<BoxingConfig> CREATOR = new Creator<BoxingConfig>() {
        @Override
        public BoxingConfig createFromParcel(Parcel source) {
            return new BoxingConfig(source);
        }

        @Override
        public BoxingConfig[] newArray(int size) {
            return new BoxingConfig[size];
        }
    };
}
