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

package com.bilibili.boxing.impl

import android.graphics.Bitmap
import android.widget.ImageView
import com.bilibili.boxing.demo.R
import com.bilibili.boxing.loader.IBoxingCallback
import com.bilibili.boxing.loader.IBoxingMediaLoader
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

/**
 * use https://github.com/square/picasso as media loader.
 * can **not** be used in Production Environment.

 * @author ChenSL
 */
class BoxingPicassoLoader : IBoxingMediaLoader {

    override fun displayThumbnail(img: ImageView, absPath: String, width: Int, height: Int) {
        val path = "file://" + absPath
        Picasso.with(img.context).load(path).placeholder(R.drawable.ic_boxing_default_image).centerCrop().resize(width, height).into(img)
    }

    override fun displayRaw(img: ImageView, absPath: String, width: Int, height: Int, callback: IBoxingCallback?) {
        val path = "file://" + absPath
        val creator = Picasso.with(img.context)
                .load(path)
        if (width > 0 && height > 0) {
            creator.transform(BitmapTransform(width, height))
        }
        creator.into(img, object : Callback {
            override fun onSuccess() {
                callback?.onSuccess()
            }

            override fun onError() {
                callback?.onFail(IllegalArgumentException())
            }
        })
    }

    private inner class BitmapTransform internal constructor(private val mMaxWidth: Int, private val mMaxHeight: Int) : Transformation {

        override fun transform(source: Bitmap): Bitmap {
            val targetWidth: Int
            val targetHeight: Int
            val aspectRatio: Double

            if (source.width > source.height) {
                targetWidth = mMaxWidth
                aspectRatio = source.height.toDouble() / source.width.toDouble()
                targetHeight = (targetWidth * aspectRatio).toInt()
            } else {
                targetHeight = mMaxHeight
                aspectRatio = source.width.toDouble() / source.height.toDouble()
                targetWidth = (targetHeight * aspectRatio).toInt()
            }

            val result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
            if (result != source) {
                source.recycle()
            }
            return result
        }

        override fun key(): String {
            return mMaxWidth.toString() + "x" + mMaxHeight
        }

    }

}
