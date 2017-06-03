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

/**
 * The base entity for media.

 * @author ChenSL
 */
abstract class BaseMedia : Parcelable{
    public enum class TYPE {
        IMAGE, VIDEO
    }

    var path: String = ""

    var id: String = ""

    var mSize: String? = ""

    constructor() {}

    constructor(id: String, path: String) {
        this.id = id
        this.path = path
    }

    abstract fun getType(): TYPE

    fun getSize(): Long {
        try {
            val result = java.lang.Long.valueOf(mSize)!!
            return if (result > 0) result else 0
        } catch (size: NumberFormatException) {
            return 0
        }

    }

    open fun setSize(size: String) {
        mSize = size
    }

    constructor(source: Parcel) {
        this.id = source.readString()
        this.path = source.readString()
        this.mSize = source.readString()
    }


    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(path)
        dest.writeString(mSize)
    }
}
