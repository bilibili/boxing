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

package com.bilibili.boxing

import android.widget.ImageView

import com.bilibili.boxing.loader.IBoxingCallback
import com.bilibili.boxing.loader.IBoxingMediaLoader

/**
 * A loader holding [IBoxingMediaLoader] to displayThumbnail medias.

 * @author ChenSL
 */
class BoxingMediaLoader private constructor() {
    lateinit var loader: IBoxingMediaLoader
        private set

    fun init(loader: IBoxingMediaLoader) {
        this.loader = loader
    }

    fun displayThumbnail(img: ImageView, path: String, width: Int, height: Int) {
        loader.displayThumbnail(img, path, width, height)
    }

    fun displayRaw(img: ImageView, path: String, width: Int, height: Int, callback: IBoxingCallback?) {
        loader.displayRaw(img, path, width, height, callback)
    }


    companion object {
        val instance = BoxingMediaLoader()
    }
}
