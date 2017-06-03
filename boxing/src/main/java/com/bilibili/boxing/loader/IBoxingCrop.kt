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

package com.bilibili.boxing.loader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment

import com.bilibili.boxing.model.config.BoxingCropOption

/**
 * Cropping interface.

 * @author ChenSL
 */
interface IBoxingCrop {

    /***
     * start crop operation.

     * @param cropConfig  [BoxingCropOption]
     * *
     * @param path        the absolute path of media.
     * *
     * @param requestCode request code for the crop.
     */
    fun onStartCrop(context: Context, fragment: Fragment, cropConfig: BoxingCropOption,
                    path: String, requestCode: Int)


    /**
     * get the result of cropping.

     * @param resultCode the code in [android.app.Activity.onActivityResult]
     * *
     * @param data       the data intent
     * *
     * @return the cropped image uri.
     */
    fun onCropFinish(resultCode: Int, data: Intent): Uri?
}
