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


import android.content.ContentResolver
import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.text.TextUtils
import com.bilibili.boxing.model.entity.BaseMedia
import com.bilibili.boxing.utils.*
import java.io.File


/**
 * Id and absolute path is necessary.Builder Mode can be used too.
 * compress image through [.compress].

 * @author ChenSL
 */
class ImageMedia : BaseMedia, Parcelable {
    var isSelected: Boolean = false

    var mThumbnailPath: String = ""

    var compressPath: String = ""

    var height: Int = 0

    var width: Int = 0

    var imageType: IMAGE_TYPE = IMAGE_TYPE.PNG

    var mMimeType: String? = ""

    enum class IMAGE_TYPE {
        PNG, JPG, GIF
    }

    constructor(id: String, imagePath: String) : super(id, imagePath)

    constructor(file: File) : super(System.currentTimeMillis().toString(), file.absolutePath) {
        this.mSize = file.length().toString()
        this.isSelected = true
    }

    constructor(builder: Builder) : super(builder.mId, builder.mImagePath) {
        this.mThumbnailPath = builder.mThumbnailPath
        this.mSize = builder.mSize
        this.height = builder.mHeight
        this.isSelected = builder.mIsSelected
        this.width = builder.mWidth
        this.mMimeType = builder.mMimeType
        this.imageType = getImageTypeByMime(builder.mMimeType)
    }

    override fun getType(): BaseMedia.TYPE {
        return BaseMedia.TYPE.IMAGE
    }

    val isGifOverSize: Boolean
        get() = isGif && getSize() > MAX_GIF_SIZE

    val isGif: Boolean
        get() = imageType == IMAGE_TYPE.GIF

    fun compress(imageCompressor: ImageCompressor): Boolean {
        return CompressTask.compress(imageCompressor, this, MAX_IMAGE_SIZE)
    }

    fun compress(imageCompressor: ImageCompressor, maxSize: Int): Boolean {
        return CompressTask.compress(imageCompressor, this, maxSize.toLong())
    }

    /**
     * get mime type displayed in database.

     * @return "image/gif" or "image/jpeg".
     */
    val mimeType: String
        get() {
            if (imageType == IMAGE_TYPE.GIF) {
                return "image/gif"
            } else if (imageType == IMAGE_TYPE.JPG) {
                return "image/jpeg"
            }
            return "image/jpeg"
        }

    private fun getImageTypeByMime(mimeType: String?): IMAGE_TYPE {
        if (!TextUtils.isEmpty(mimeType)) {
            if ("image/gif" == mimeType) {
                return IMAGE_TYPE.GIF
            } else if ("image/png" == mimeType) {
                return IMAGE_TYPE.PNG
            } else {
                return IMAGE_TYPE.JPG
            }
        }
        return IMAGE_TYPE.PNG
    }

    fun removeExif() {
        BoxingExifHelper.removeExif(path)
    }

    /**
     * save image to MediaStore.
     */
    fun saveMediaStore(cr: ContentResolver?) {
        BoxingExecutor.instance.runWorker(Runnable {
            if (cr != null && !TextUtils.isEmpty(id)) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.TITLE, id)
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                values.put(MediaStore.Images.Media.DATA, path)
                cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        })

    }

    override fun setSize(size: String) {
        mSize = size
    }

    override fun toString(): String {
        return "ImageMedia{" +
                ", mThumbnailPath='" + mThumbnailPath + '\'' +
                ", mCompressPath='" + compressPath + '\'' +
                ", mSize='" + mSize + '\'' +
                ", mHeight=" + height +
                ", mWidth=" + width
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }

    val thumbnailPath: String
        get() {
            if (BoxingFileHelper.isFileValid(mThumbnailPath)) {
                return mThumbnailPath
            } else if (BoxingFileHelper.isFileValid(compressPath)) {
                return compressPath
            }
            return path
        }

    override fun equals(obj: Any?): Boolean {
        if (this === obj)
            return true
        if (obj == null)
            return false
        if (javaClass != obj.javaClass)
            return false
        val other = obj as ImageMedia?
        return !(TextUtils.isEmpty(path) || TextUtils.isEmpty(other!!.path)) && this.path == other.path
    }

    class Builder(val mId: String, val mImagePath: String) {
        var mIsSelected: Boolean = false
        var mThumbnailPath: String = ""
        var mSize: String? = null
        var mHeight: Int = 0
        var mWidth: Int = 0
        var mMimeType: String? = null

        fun setSelected(selected: Boolean): Builder {
            this.mIsSelected = selected
            return this
        }

        fun setThumbnailPath(thumbnailPath: String): Builder {
            mThumbnailPath = thumbnailPath
            return this
        }

        fun setHeight(height: Int): Builder {
            mHeight = height
            return this
        }

        fun setWidth(width: Int): Builder {
            mWidth = width
            return this
        }

        fun setMimeType(mimeType: String): Builder {
            mMimeType = mimeType
            return this
        }

        fun setSize(size: String): Builder {
            this.mSize = size
            return this
        }

        fun build(): ImageMedia {
            return ImageMedia(this)
        }
    }

    companion object {
        private val MAX_GIF_SIZE = 1024 * 1024L

        private val MAX_IMAGE_SIZE = 1024 * 1024L

        @JvmField val CREATOR: Parcelable.Creator<ImageMedia> = object : Parcelable.Creator<ImageMedia> {
            override fun createFromParcel(source: Parcel): ImageMedia = ImageMedia(source)
            override fun newArray(size: Int): Array<ImageMedia?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : super(source) {
        this.isSelected = source.readByte() == 1.toByte()
        mThumbnailPath = source.readString()
        compressPath = source.readString()
        height = source.readInt()
        width = source.readInt()
        imageType = IMAGE_TYPE.valueOf(source.readString())
        mMimeType = source.readString()
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeByte(if (this.isSelected) 1.toByte() else 0.toByte())
        dest.writeString(this.mThumbnailPath)
        dest.writeString(this.compressPath)
        dest.writeInt(this.height)
        dest.writeInt(this.width)
        dest.writeString(this.imageType.name)
        dest.writeString(this.mMimeType)

    }
}
