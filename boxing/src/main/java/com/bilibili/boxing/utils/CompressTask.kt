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
import com.bilibili.boxing.model.entity.impl.ImageMedia
import java.io.File
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

/**
 * A compress task for [ImageMedia]
 * @author ChenSL
 */
object CompressTask {
    fun compress(context: Context, image: ImageMedia): Boolean {
        return compress(ImageCompressor(context), image, ImageCompressor.MAX_LIMIT_SIZE_LONG)
    }

    fun compress(imageCompressor: ImageCompressor?, image: ImageMedia?, maxSize: Long): Boolean {
        if (imageCompressor == null || image == null || maxSize <= 0) {
            return false
        }
        val task = BoxingExecutor.instance.runWorker(Callable {
            val path = image.path
            val compressSaveFile = imageCompressor.getCompressOutFile(path)
            val needCompressFile = File(path)
            if (BoxingFileHelper.isFileValid(compressSaveFile)) {
                image.compressPath = compressSaveFile.absolutePath
                return@Callable true
            }
            if (!BoxingFileHelper.isFileValid(needCompressFile)) {
                return@Callable false
            } else if (image.getSize() < maxSize) {
                image.compressPath = path
                return@Callable true
            } else {
                try {
                    val result = imageCompressor.compress(needCompressFile)
                    val suc = BoxingFileHelper.isFileValid(result)
                    image.compressPath = if (suc) result.absolutePath else ""
                    return@Callable suc
                } catch (e: IOException) {
                    image.compressPath = ""
                    BoxingLog.d("image compress fail!")
                } catch (e: OutOfMemoryError) {
                    image.compressPath = ""
                    BoxingLog.d("image compress fail!")
                } catch (e: NullPointerException) {
                    image.compressPath = ""
                    BoxingLog.d("image compress fail!")
                } catch (e: IllegalArgumentException) {
                    image.compressPath = ""
                    BoxingLog.d("image compress fail!")
                }

            }
            false
        })
        try {
            return task != null && task.get()
        } catch (ignore: InterruptedException) {
            return false
        } catch (ignore: ExecutionException) {
            return false
        }

    }

}
