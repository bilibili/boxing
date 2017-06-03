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

import android.content.Context
import android.os.Environment
import android.text.TextUtils

import java.io.File
import java.util.concurrent.ExecutionException

/**
 * A file helper to make thing easier.

 * @author ChenSL
 */
object BoxingFileHelper {
    val DEFAULT_SUB_DIR = "/bili/boxing"

    @Throws(ExecutionException::class, InterruptedException::class)
    fun createFile(path: String): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        val file = File(path)
        if (file.exists()) {
            return true
        }
        return file.mkdirs()

    }

    fun getCacheDir(context: Context): String? {
        val cacheDir = context.applicationContext.cacheDir
        if (cacheDir == null) {
            BoxingLog.d("cache dir do not exist.")
            return null
        }
        val result = cacheDir.absolutePath + "/boxing"
        try {
            BoxingFileHelper.createFile(result)
        } catch (e: ExecutionException) {
            BoxingLog.d("cache dir $result not exist")
            return null
        } catch (e: InterruptedException) {
            BoxingLog.d("cache dir $result not exist")
            return null
        }

        BoxingLog.d("cache dir is: " + result)
        return result
    }

    val boxingPathInDCIM: String?
        get() = getExternalDCIM(null)

    fun getExternalDCIM(subDir: String?): String? {
        var result: String? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) ?: return result
            var dir = "/bili/boxing"
            if (!TextUtils.isEmpty(subDir)) {
                dir = subDir!!
            }
            result = file.absolutePath + dir
            BoxingLog.d("external DCIM is: " + result)
            return result
        }
        BoxingLog.d("external DCIM do not exist.")
        return result
    }

    fun isFileValid(path: String): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        val file = File(path)
        return isFileValid(file)
    }

    fun isFileValid(file: File): Boolean {
        return file.exists() && file.isFile && file.length() > 0 && file.canRead()
    }
}
