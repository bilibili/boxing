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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.app.Fragment

import com.bilibili.boxing.loader.IBoxingCrop
import com.bilibili.boxing.model.config.BoxingCropOption
import com.yalantis.ucrop.UCrop

/**
 * use Ucrop(https://github.com/Yalantis/uCrop) as the implement for [IBoxingCrop]

 * @author ChenSL
 */
class BoxingUcrop : IBoxingCrop {

    override fun onStartCrop(context: Context, fragment: Fragment, cropConfig: BoxingCropOption,
                             path: String, requestCode: Int) {
        val uri = Uri.Builder()
                .scheme("file")
                .appendPath(path)
                .build()
        val crop = UCrop.Options()
        // do not copy exif information to crop pictures
        // because png do not have exif and png is not Distinguishable
        crop.setCompressionFormat(Bitmap.CompressFormat.PNG)
        crop.withMaxResultSize(cropConfig.maxWidth, cropConfig.maxHeight)
        crop.withAspectRatio(cropConfig.aspectRatioX, cropConfig.aspectRatioY)

        UCrop.of(uri, cropConfig.destination!!)
                .withOptions(crop)
                .start(context, fragment, requestCode)
    }

    override fun onCropFinish(resultCode: Int, data: Intent): Uri? {
        val throwable = UCrop.getError(data)
        if (throwable != null) {
            return null
        }
        return UCrop.getOutput(data)
    }
}
