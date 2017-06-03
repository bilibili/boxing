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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment

import com.bilibili.boxing.loader.IBoxingCrop
import com.bilibili.boxing.model.config.BoxingCropOption

/**
 * A loader holding [IBoxingCrop] to crop images.

 * @author ChenSL
 */
class BoxingCrop private constructor() {
    lateinit var crop: IBoxingCrop
        private set

    fun init(loader: IBoxingCrop) {
        this.crop = loader
    }

    fun onStartCrop(activity: Activity, fragment: Fragment, cropConfig: BoxingCropOption,
                    path: String, requestCode: Int) {
        crop.onStartCrop(activity, fragment, cropConfig, path, requestCode)
    }

    fun onCropFinish(resultCode: Int, data: Intent): Uri? {
        return crop.onCropFinish(resultCode, data)
    }

    companion object {
        val instance = BoxingCrop()
    }
}
