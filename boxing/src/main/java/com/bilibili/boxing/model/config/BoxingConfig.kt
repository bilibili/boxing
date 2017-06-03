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

package com.bilibili.boxing.model.config

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import com.bilibili.boxing.model.config.BoxingConfig.Mode

/**
 * The pick config.<br></br>
 * 1.[Mode] is necessary. <br></br>
 * 2.specify functions: camera, gif, paging. <br></br>
 * calling [.needCamera] to displayThumbnail a camera icon. <br></br>
 * calling [.needGif] to displayThumbnail gif photos. <br></br>
 * calling [.needPaging] to create load medias page by page, by default is true.

 * @author ChenSL
 */
class BoxingConfig : Parcelable {
    var mode = Mode.SINGLE_IMG

    var viewMode = ViewMode.PREVIEW
        private set

    var cropOption: BoxingCropOption? = null
        private set

    /**
     * get the image drawable resource by [BoxingConfig.withMediaPlaceHolderRes].
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    var mediaPlaceHolderRes: Int = 0
        private set

    /**
     * get the media checked drawable resource by [BoxingConfig.withMediaCheckedRes].
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    var mediaCheckedRes: Int = 0
        private set

    /**
     * get the media unchecked drawable resource by [BoxingConfig.withMediaUncheckedRes] (int)}.
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    var mediaUnCheckedRes: Int = 0
        private set

    /**
     * get the album drawable resource by [BoxingConfig.withAlbumPlaceHolderRes].
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    var albumPlaceHolderRes: Int = 0
        private set

    /**
     * get the video drawable resource by [BoxingConfig.withVideoDurationRes].
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    var videoDurationRes: Int = 0
        private set

    /**
     * get the media unchecked drawable resource by [BoxingConfig.withMediaPlaceHolderRes].
     * @return >0, set a valid drawable resource; otherwise without a placeholder.
     */
    var cameraRes: Int = 0
        private set

    var isNeedCamera: Boolean = false
        private set

    var isNeedGif: Boolean = false
        private set

    var isNeedPaging = true
        private set

    private
    var mMaxCount = DEFAULT_SELECTED_COUNT

    enum class Mode {
        SINGLE_IMG, MULTI_IMG, VIDEO
    }

    enum class ViewMode {
        PREVIEW, EDIT, PRE_EDIT
    }

    constructor() {}

    constructor(mode: Mode) {
        this.mode = mode
    }

    /**
     * get the max count set by [.withMaxCount], otherwise return 9.
     */
    val maxCount: Int
        get() {
            if (mMaxCount > 0) {
                return mMaxCount
            }
            return DEFAULT_SELECTED_COUNT
        }

    val isNeedLoading: Boolean
        get() = viewMode == ViewMode.EDIT

    val isNeedEdit: Boolean
        get() = viewMode != ViewMode.PREVIEW

    val isVideoMode: Boolean
        get() = mode == Mode.VIDEO

    val isMultiImageMode: Boolean
        get() = mode == Mode.MULTI_IMG

    val isSingleImageMode: Boolean
        get() = mode == Mode.SINGLE_IMG

    /**
     * call this means gif is needed.
     */
    fun needGif(): BoxingConfig {
        this.isNeedGif = true
        return this
    }

    /**
     * set the camera res.
     */
    fun needCamera(@DrawableRes cameraRes: Int): BoxingConfig {
        this.cameraRes = cameraRes
        this.isNeedCamera = true
        return this
    }

    /**
     * call this means paging is needed,by default is true.
     */
    fun needPaging(needPaging: Boolean): BoxingConfig {
        this.isNeedPaging = needPaging
        return this
    }

    fun withViewer(viewMode: ViewMode): BoxingConfig {
        this.viewMode = viewMode
        return this
    }

    fun withCropOption(cropOption: BoxingCropOption): BoxingConfig {
        this.cropOption = cropOption
        return this
    }

    /**
     * set the max count of selected medias in [Mode.MULTI_IMG]
     * @param count max count
     */
    fun withMaxCount(count: Int): BoxingConfig {
        if (count < 1) {
            return this
        }
        this.mMaxCount = count
        return this
    }

    /**
     * set the image placeholder, default 0
     */
    fun withMediaPlaceHolderRes(@DrawableRes mediaPlaceHolderRes: Int): BoxingConfig {
        this.mediaPlaceHolderRes = mediaPlaceHolderRes
        return this
    }

    /**
     * set the image placeholder, otherwise use default drawable.
     */
    fun withMediaCheckedRes(@DrawableRes mediaCheckedResRes: Int): BoxingConfig {
        this.mediaCheckedRes = mediaCheckedResRes
        return this
    }

    /**
     * set the image placeholder, otherwise use default drawable.
     */
    fun withMediaUncheckedRes(@DrawableRes mediaUncheckedRes: Int): BoxingConfig {
        this.mediaUnCheckedRes = mediaUncheckedRes
        return this
    }

    /**
     * set the album placeholder, default 0
     */
    fun withAlbumPlaceHolderRes(@DrawableRes albumPlaceHolderRes: Int): BoxingConfig {
        this.albumPlaceHolderRes = albumPlaceHolderRes
        return this
    }

    /**
     * set the video duration resource in video mode, default 0
     */
    fun withVideoDurationRes(@DrawableRes videoDurationRes: Int): BoxingConfig {
        this.videoDurationRes = videoDurationRes
        return this
    }

    override fun toString(): String {
        return "BoxingConfig{" +
                "mMode=" + mode +
                ", mViewMode=" + viewMode +
                '}'
    }

    companion object {
        val DEFAULT_SELECTED_COUNT = 9

        @JvmField val CREATOR: Parcelable.Creator<BoxingConfig> = object : Parcelable.Creator<BoxingConfig> {
            override fun createFromParcel(source: Parcel): BoxingConfig = BoxingConfig(source)
            override fun newArray(size: Int): Array<BoxingConfig?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) {
        mode = Mode.values()[source.readInt()]
        viewMode = ViewMode.values()[source.readInt()]
        cropOption = source.readParcelable(BoxingCropOption.CREATOR.javaClass.classLoader)
        mediaPlaceHolderRes = source.readInt()
        mediaCheckedRes = source.readInt()
        mediaUnCheckedRes = source.readInt()
        albumPlaceHolderRes = source.readInt()
        videoDurationRes = source.readInt()
        cameraRes = source.readInt()
        isNeedCamera = source.readInt() == 1
        isNeedGif = source.readInt() == 1
        isNeedPaging = source.readInt() == 1
        mMaxCount = source.readInt()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(mode.ordinal)
        dest.writeInt(viewMode.ordinal)
        dest.writeParcelable(cropOption, flags)
        dest.writeInt(mediaPlaceHolderRes)
        dest.writeInt(mediaCheckedRes)
        dest.writeInt(mediaUnCheckedRes)
        dest.writeInt(albumPlaceHolderRes)
        dest.writeInt(videoDurationRes)
        dest.writeInt(cameraRes)
        dest.writeInt(if(isNeedCamera) 1 else 0)
        dest.writeInt(if(isNeedGif) 1 else 0)
        dest.writeInt(if(isNeedPaging) 1 else 0)
        dest.writeInt(mMaxCount)

    }
}
