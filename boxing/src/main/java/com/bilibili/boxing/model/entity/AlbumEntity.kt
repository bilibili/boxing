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

package com.bilibili.boxing.model.entity

import android.os.Parcel
import android.os.Parcelable
import java.util.*


/**
 * An entity for album.

 * @author ChenSL
 */
class AlbumEntity : Parcelable {

    var mCount: Int = 0
    var mIsSelected: Boolean = false

    var mBucketId: String = ""
    var mBucketName: String
    var mImageList: MutableList<BaseMedia>

    constructor() {
        mCount = 0
        mBucketName = DEFAULT_NAME
        mImageList = ArrayList<BaseMedia>()
        mIsSelected = false
    }

    fun hasImages(): Boolean {
        return mImageList.isNotEmpty()
    }

    override fun toString(): String {
        return "AlbumEntity{" +
                "mCount=" + mCount +
                ", mBucketName='" + mBucketName + '\'' +
                ", mImageList=" + mImageList +
                '}'
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.mBucketId)
        dest.writeInt(this.mCount)
        dest.writeString(this.mBucketName)
        dest.writeList(this.mImageList)
        dest.writeByte(if (this.mIsSelected) 1.toByte() else 0.toByte())
    }

    protected constructor(`in`: Parcel) {
        this.mBucketId = `in`.readString()
        this.mCount = `in`.readInt()
        this.mBucketName = `in`.readString()
        this.mImageList = ArrayList<BaseMedia>()
        `in`.readList(this.mImageList, BaseMedia::class.java.classLoader)
        this.mIsSelected = `in`.readByte().toInt() != 0
    }

    companion object {
        val DEFAULT_NAME = ""

        fun createDefaultAlbum(): AlbumEntity {
            val result = AlbumEntity()
            result.mBucketId = DEFAULT_NAME
            result.mBucketName = "所有相片"
            result.mIsSelected = true
            return result
        }

        val CREATOR: Parcelable.Creator<AlbumEntity> = object : Parcelable.Creator<AlbumEntity> {
            override fun createFromParcel(source: Parcel): AlbumEntity {
                return AlbumEntity(source)
            }

            override fun newArray(size: Int): Array<AlbumEntity?> {
                return arrayOfNulls(size)
            }
        }
    }
}
