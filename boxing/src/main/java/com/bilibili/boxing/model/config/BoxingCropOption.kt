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

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

/**
 * The cropping config, a cropped photo uri is needed at least.

 * @author ChenSL
 */
class BoxingCropOption : Parcelable {
    var destination: Uri? = null
        private set
    var aspectRatioX: Float = 0.toFloat()
        private set
    var aspectRatioY: Float = 0.toFloat()
        private set
    var maxWidth: Int = 0
        private set
    var maxHeight: Int = 0
        private set

    constructor(destination: Uri) {
        this.destination = destination
    }

    fun aspectRatio(x: Float, y: Float): BoxingCropOption {
        this.aspectRatioX = x
        this.aspectRatioY = y
        return this
    }

    fun useSourceImageAspectRatio(): BoxingCropOption {
        this.aspectRatioX = 0f
        this.aspectRatioY = 0f
        return this
    }

    fun withMaxResultSize(width: Int, height: Int): BoxingCropOption {
        this.maxWidth = width
        this.maxHeight = height
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(this.destination, flags)
        dest.writeFloat(this.aspectRatioX)
        dest.writeFloat(this.aspectRatioY)
        dest.writeInt(this.maxWidth)
        dest.writeInt(this.maxHeight)
    }

    internal constructor(`in`: Parcel) {
        this.destination = `in`.readParcelable<Uri>(Uri::class.java.classLoader)
        this.aspectRatioX = `in`.readFloat()
        this.aspectRatioY = `in`.readFloat()
        this.maxWidth = `in`.readInt()
        this.maxHeight = `in`.readInt()
    }

    companion object {

        fun with(destination: Uri): BoxingCropOption {
            return BoxingCropOption(destination)
        }

        val CREATOR: Parcelable.Creator<BoxingCropOption> = object : Parcelable.Creator<BoxingCropOption> {
            override fun createFromParcel(source: Parcel): BoxingCropOption {
                return BoxingCropOption(source)
            }

            override fun newArray(size: Int): Array<BoxingCropOption?> {
                return arrayOfNulls(size)
            }
        }
    }
}