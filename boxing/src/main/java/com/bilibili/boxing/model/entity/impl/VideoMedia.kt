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

package com.bilibili.boxing.model.entity.impl

import android.os.Parcel
import android.os.Parcelable
import com.bilibili.boxing.model.entity.BaseMedia
import java.util.*


/**
 * Entity represent a Video.

 * @author ChenSL
 */
class VideoMedia : BaseMedia {

    var title: String? = null
    private var mDuration: String? = null
    var dateTaken: String? = null
        private set
    var mimeType: String? = null
        private set

    private constructor() {}

    override fun getType(): BaseMedia.TYPE {
        return BaseMedia.TYPE.VIDEO
    }

    constructor(builder: Builder) : super(builder.mId, builder.mPath) {
        this.title = builder.mTitle
        this.mDuration = builder.mDuration
        this.mSize = builder.mSize
        this.dateTaken = builder.mDateTaken
        this.mimeType = builder.mMimeType
    }

    var duration: String
        get() {
            try {
                val duration = java.lang.Long.parseLong(mDuration)
                return formatTimeWithMin(duration)
            } catch (e: NumberFormatException) {
                return "0:00"
            }

        }
        set(duration) {
            mDuration = duration
        }

    fun formatTimeWithMin(duration: Long): String {
        if (duration <= 0) {
            return String.format(Locale.US, "%02d:%02d", 0, 0)
        }
        val totalSeconds = duration / 1000

        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d", hours * 60 + minutes,
                    seconds)
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    val sizeByUnit: String
        get() {
            val size = getSize().toDouble()
            if (size == 0.0) {
                return "0K"
            }
            if (size >= MB) {
                val sizeInM = size / MB
                return String.format(Locale.getDefault(), "%.1f", sizeInM) + "M"
            }
            val sizeInK = size / 1024
            return String.format(Locale.getDefault(), "%.1f", sizeInK) + "K"
        }

    class Builder(val mId: String, val mPath: String) {
        var mTitle: String? = null
        var mDuration: String? = null
        var mSize: String? = null
        var mDateTaken: String? = null
        var mMimeType: String? = null

        fun setTitle(title: String): Builder {
            this.mTitle = title
            return this
        }

        fun setDuration(duration: String): Builder {
            this.mDuration = duration
            return this
        }

        fun setSize(size: String): Builder {
            this.mSize = size
            return this
        }

        fun setDataTaken(dateTaken: String): Builder {
            this.mDateTaken = dateTaken
            return this
        }

        fun setMimeType(type: String): Builder {
            this.mMimeType = type
            return this
        }


        fun build(): VideoMedia {
            return VideoMedia(this)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(this.title)
        dest.writeString(this.mDuration)
        dest.writeString(this.dateTaken)
        dest.writeString(this.mimeType)
    }

    protected constructor(`in`: Parcel) : super(`in`) {
        this.title = `in`.readString()
        this.mDuration = `in`.readString()
        this.dateTaken = `in`.readString()
        this.mimeType = `in`.readString()
    }

    companion object {
        private val MB = (1024 * 1024).toLong()

        val CREATOR: Parcelable.Creator<VideoMedia> = object : Parcelable.Creator<VideoMedia> {
            override fun createFromParcel(source: Parcel): VideoMedia {
                return VideoMedia(source)
            }

            override fun newArray(size: Int): Array<VideoMedia?> {
                return arrayOfNulls(size)
            }
        }
    }
}
