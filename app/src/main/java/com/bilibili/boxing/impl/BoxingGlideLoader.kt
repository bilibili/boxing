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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

/**
 * use https://github.com/bumptech/glide as media loader.
 * can **not** be used in Production Environment.

 * @author ChenSL
 */
class BoxingGlideLoader : IBoxingMediaLoader {

    override fun displayThumbnail(img: ImageView, absPath: String, width: Int, height: Int) {
        val path = "file://" + absPath
        try {
            // https://github.com/bumptech/glide/issues/1531
            Glide.with(img.context).load(path).placeholder(R.drawable.ic_boxing_default_image).crossFade().centerCrop().override(width, height).into(img)
        } catch (ignore: IllegalArgumentException) {
        }

    }

    override fun displayRaw(img: ImageView, absPath: String, width: Int, height: Int, callback: IBoxingCallback?) {
        val path = "file://" + absPath
        val request = Glide.with(img.context)
                .load(path)
                .asBitmap()
        if (width > 0 && height > 0) {
            request.override(width, height)
        }
        request.listener(object : RequestListener<String, Bitmap> {
            override fun onException(e: Exception, model: String, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                callback?.onFail(e)
                return true
            }

            override fun onResourceReady(resource: Bitmap?, model: String, target: Target<Bitmap>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                if (resource != null) {
                    img.setImageBitmap(resource)
                    callback?.onSuccess()
                    return true
                }
                return false
            }
        }).into(img)

    }

}
