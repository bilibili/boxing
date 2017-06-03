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

package com.bilibili.boxing.utils

import android.media.ExifInterface
import android.os.Build
import android.text.TextUtils

import java.io.IOException

/**
 * @author ChenSL
 */

object BoxingExifHelper {

    fun removeExif(path: String) {
        if (!TextUtils.isEmpty(path)) {
            return
        }
        val exifInterface: ExifInterface
        try {
            exifInterface = ExifInterface(path)
        } catch (ignore: IOException) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            exifInterface.setAttribute(ExifInterface.TAG_ARTIST, "")
            exifInterface.setAttribute(ExifInterface.TAG_RESOLUTION_UNIT, "0")
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, "")
            exifInterface.setAttribute(ExifInterface.TAG_MAKER_NOTE, "0")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            exifInterface.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, "")
        }
        exifInterface.setAttribute(ExifInterface.TAG_MAKE, "")
        exifInterface.setAttribute(ExifInterface.TAG_MODEL, "")
        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "0")

        exifInterface.setAttribute(ExifInterface.TAG_DATETIME, "")
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "")
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "")

        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "")

    }

    internal fun getRotateDegree(path: String): Int {
        var result = 0
        try {
            val exif = ExifInterface(path)
            val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> result = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> result = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> result = 270
            }
        } catch (ignore: IOException) {
            return 0
        }

        return result
    }

}
