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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.text.TextUtils
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and


/**
 * @author ChenSL
 */
class ImageCompressor {

    private var mOutFileFile: File? = null

    constructor(cachedRootDir: File) {
        mOutFileFile = File(cachedRootDir.absolutePath + File.separator + ".compress" + File.separator)
    }

    constructor(context: Context) {
        val rootDir = BoxingFileHelper.getCacheDir(context)
        if (TextUtils.isEmpty(rootDir)) {
            throw IllegalStateException("the cache dir is null")
        }
        mOutFileFile = File(rootDir + File.separator + ".compress" + File.separator)
    }

    @Throws(IOException::class, NullPointerException::class, IllegalArgumentException::class)
    fun compress(file: File): File {
        if (!file.exists()) {
            throw IllegalArgumentException("file not found : " + file.absolutePath)
        }
        if (!isLegalFile(file)) {
            throw IllegalArgumentException("file is not a legal file : " + file.absolutePath)
        }
        if (mOutFileFile == null) {
            throw NullPointerException("the external cache dir is null")
        }
        val checkOptions = BitmapFactory.Options()
        checkOptions.inJustDecodeBounds = true
        val absPath = file.absolutePath
        val angle = BoxingExifHelper.getRotateDegree(absPath)
        BitmapFactory.decodeFile(absPath, checkOptions)

        if (checkOptions.outWidth <= 0 || checkOptions.outHeight <= 0) {
            throw IllegalArgumentException("file is not a legal bitmap with 0 with or 0 height : " + file.absolutePath)
        }
        val width = checkOptions.outWidth
        val height = checkOptions.outHeight
        val outFile = createCompressFile(file)
        if (!isLargeRatio(width, height)) {
            val display = getCompressDisplay(width, height)
            val bitmap = compressDisplay(absPath, display[0], display[1])
            val rotatedBitmap = rotatingImage(angle, bitmap)
            if (bitmap != rotatedBitmap) {
                bitmap.recycle()
            }
            saveBitmap(rotatedBitmap, outFile)
            rotatedBitmap.recycle()
            compressQuality(outFile, MAX_LIMIT_SIZE, 20)
        } else {
            if (checkOptions.outHeight >= MAX_HEIGHT && checkOptions.outWidth >= MAX_WIDTH) {
                checkOptions.inSampleSize = 2
            }
            checkOptions.inJustDecodeBounds = false
            val originBitmap = BitmapFactory.decodeFile(absPath, checkOptions)
            val rotatedBitmap = rotatingImage(angle, originBitmap)
            if (originBitmap != rotatedBitmap) {
                originBitmap.recycle()
            }
            saveBitmap(originBitmap, outFile)
            rotatedBitmap.recycle()
            compressQuality(outFile, MAX_LIMIT_SIZE_LONG, 50)
        }
        BoxingLog.d("compress suc: " + outFile.absolutePath)
        return outFile
    }

    private fun rotatingImage(angle: Int, bitmap: Bitmap): Bitmap {
        if (angle == 0) {
            return bitmap
        }
        //rotate image
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())

        //create a new image
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    @Throws(IOException::class)
    private fun saveBitmap(bitmap: Bitmap, outFile: File) {
        val fos = FileOutputStream(outFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        try {
            fos.flush()
        } finally {
            try {
                fos.close()
            } catch (e: IOException) {
                BoxingLog.d("IOException when saving a bitmap")
            }

        }
    }

    /**
     * @param width  must > 0
     * *
     * @param height must > 0
     */
    private fun getCompressDisplay(width: Int, height: Int): IntArray {
        var thumbWidth = if (width % 2 == 1) width + 1 else width
        var thumbHeight = if (height % 2 == 1) height + 1 else height
        val results = intArrayOf(thumbWidth, thumbHeight)

        val width1 = if (thumbWidth > thumbHeight) thumbHeight else thumbWidth
        val height1 = if (thumbWidth > thumbHeight) thumbWidth else thumbHeight
        val scale = width1.toFloat() / height1
        if (scale <= 1 && scale >= 0.5625) {
            if (height1 < 1664) {
                thumbWidth = width1
                thumbHeight = height1
            } else if (height1 in 1664..4989) {
                thumbWidth = width1 / 2
                thumbHeight = height1 / 2
            } else if (height1 in 4990..10239) {
                thumbWidth = width1 / 4
                thumbHeight = height1 / 4
            } else {
                val multiple = if (height1 / 1280 == 0) 1 else height1 / 1280
                thumbWidth = width1 / multiple
                thumbHeight = height1 / multiple
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (height1 < 1280) {
                thumbWidth = width1
                thumbHeight = height1
            } else {
                val multiple = if (height1 / 1280 == 0) 1 else height1 / 1280
                thumbWidth = width1 / multiple
                thumbHeight = height1 / multiple
            }
        } else {
            val multiple = Math.ceil(height1 / (1280.0 / scale)).toInt()
            thumbWidth = width1 / multiple
            thumbHeight = height1 / multiple
        }
        results[0] = thumbWidth
        results[1] = thumbHeight
        return results
    }

    /**
     * @param width  must > 0
     * *
     * @param height must > 0
     */
    private fun compressDisplay(imagePath: String, width: Int, height: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)

        val outH = options.outHeight
        val outW = options.outWidth
        var inSampleSize = 1

        if (outH > height || outW > width) {
            val halfH = outH / 2
            val halfW = outW / 2
            while (halfH / inSampleSize > height && halfW / inSampleSize > width) {
                inSampleSize *= 2
            }
        }

        options.inSampleSize = inSampleSize

        options.inJustDecodeBounds = false

        val heightRatio = Math.ceil((options.outHeight / height.toFloat()).toDouble()).toInt()
        val widthRatio = Math.ceil((options.outWidth / width.toFloat()).toDouble()).toInt()

        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                options.inSampleSize = heightRatio
            } else {
                options.inSampleSize = widthRatio
            }
        }
        options.inJustDecodeBounds = false

        return BitmapFactory.decodeFile(imagePath, options)
    }

    @Throws(IOException::class)
    private fun compressQuality(outFile: File, maxSize: Long, maxQuality: Int) {
        val length = outFile.length()
        var quality = 90
        if (length > maxSize) {
            val bos = ByteArrayOutputStream()
            BoxingLog.d("source file size : " + outFile.length() + ",path : " + outFile)
            while (true) {
                compressPhotoByQuality(outFile, bos, quality)
                val size = bos.size().toLong()
                BoxingLog.d("compressed file size : " + size)
                if (quality <= maxQuality) {
                    break
                }
                if (size < maxSize) {
                    break
                } else {
                    quality -= 10
                    bos.reset()
                }
            }
            val fos = FileOutputStream(outFile)
            bos.writeTo(fos)
            bos.flush()
            fos.close()
            bos.close()
        }
    }

    @Throws(IOException::class, OutOfMemoryError::class)
    private fun compressPhotoByQuality(file: File, os: OutputStream, quality: Int) {
        BoxingLog.d("start compress quality... ")
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os)
            bitmap.recycle()
        } else {
            throw NullPointerException("bitmap is null when compress by quality")
        }
    }

    @Throws(IOException::class)
    private fun createCompressFile(file: File): File {
        val outFile = getCompressOutFile(file)
        if (!mOutFileFile!!.exists()) {
            mOutFileFile!!.mkdirs()
        }
        BoxingLog.d("compress out file : " + outFile)
        outFile.createNewFile()
        return outFile
    }

    fun getCompressOutFile(file: File): File {
        return File(getCompressOutFilePath(file))
    }

    fun getCompressOutFile(filePth: String): File {
        return File(getCompressOutFilePath(filePth))
    }

    fun getCompressOutFilePath(file: File): String {
        return getCompressOutFilePath(file.absolutePath)
    }

    fun getCompressOutFilePath(filePath: String): String {
        return mOutFileFile.toString() + File.separator + COMPRESS_FILE_PREFIX + signMD5(filePath.toByteArray()) + ".jpg"
    }

    fun signMD5(source: ByteArray): String? {
        try {
            val digest = MessageDigest.getInstance("MD5")
            return signDigest(source, digest)
        } catch (e: NoSuchAlgorithmException) {
            BoxingLog.d("have no md5")
        }

        return null
    }

    private fun signDigest(source: ByteArray, digest: MessageDigest): String {
        digest.update(source)
        val data = digest.digest()
        val j = data.size
        val str = CharArray(j * 2)
        var k = 0
        for (byte0 in data) {
            str[k++] = HEX_DIGITS[(byte0.toInt() ushr 4) and 0xf]
            str[k++] = HEX_DIGITS[(byte0 and 0xf).toInt()]
        }
        return String(str).toLowerCase()
    }

    private fun isLargeRatio(width: Int, height: Int): Boolean {
        return width / height >= 3 || height / width >= 3
    }

    private fun isLegalFile(file: File?): Boolean {
        return file != null && file.exists() && file.isFile && file.length() > 0
    }

    companion object {
        val MAX_LIMIT_SIZE_LONG = 1024 * 1024L

        private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        private val MAX_WIDTH = 3024
        private val MAX_HEIGHT = 4032
        private val MAX_LIMIT_SIZE = 300 * 1024L

        private val COMPRESS_FILE_PREFIX = "compress-"
    }
}
